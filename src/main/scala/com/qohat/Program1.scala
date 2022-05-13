package com.qohat

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import com.qohat.model.ioDecoder.jsonDecode
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.json.Fs2JsonEncoder
import dev.profunktor.fs2rabbit.model.AckResult.{ Ack, NAck }
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.{ LongVal, StringVal }
import dev.profunktor.fs2rabbit.model._
import fs2.{ Pipe, Stream }
import io.circe._

import java.nio.charset.StandardCharsets.UTF_8

class Flow[F[_]: Async, A](
    consumer: Stream[F, AmqpEnvelope[A]],
    acker: AckResult => F[Unit],
    logger: Pipe[F, AmqpEnvelope[A], AckResult],
    publisher: AmqpMessage[String] => F[Unit]
) {

  import io.circe.generic.auto._

  case class Address(number: Int, streetName: String)
  case class Person(id: Long, name: String, address: Address)

  private val jsonEncoder = new Fs2JsonEncoder
  import jsonEncoder.jsonEncode

  val jsonPipe: Pipe[F, AmqpMessage[Person], AmqpMessage[String]] = _.map(jsonEncode[Person])

  val processorSync: Pipe[F, (Person, DeliveryTag), Unit] = ???

  val errorSync: Pipe[F, Error, Unit] = ???

  val jsonPipe: Pipe[F, AmqpMessage[Person], AmqpMessage[String]] = _.map(jsonEncode[Person])

  val simpleMessage =
    AmqpMessage("Hey!", AmqpProperties(headers = Map("demoId" -> LongVal(123), "app" -> StringVal("fs2RabbitDemo"))))
  val classMessage = AmqpMessage(Person(1L, "Sherlock", Address(212, "Baker St")), AmqpProperties.empty)

  val flow: Stream[F, Unit] =
    Stream(
      Stream(simpleMessage).covary[F].evalMap(publisher),
      Stream(classMessage).covary[F].through(jsonPipe).evalMap(publisher),
      consumer
        .map(jsonDecode[Person])
        .flatMap {
          case (Left(error), tag) =>
            (Stream.eval(Async[F].delay(error)).through(errorSync)).as(NAck(tag)).evalMap(acker)
          case (Right(msg), tag) => Stream.eval(Async[F].delay((msg, tag))).through(processorSync)
        }
        .through(logger)
    ).parJoin(3)

}

class AckerConsumerDemo[F[_]: Async](R: RabbitClient[F]) {

  private val queueName    = QueueName("testQ")
  private val exchangeName = ExchangeName("testEX")
  private val routingKey   = RoutingKey("testRK")
  implicit val stringMessageEncoder =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(UTF_8)).pure[F])

  def logPipe: Pipe[F, AmqpEnvelope[String], AckResult] = _.evalMap { amqpMsg =>
    Sync[F].delay(println(s"Consumed: $amqpMsg")).as(Ack(amqpMsg.deliveryTag))
  }

  val publishingFlag: PublishingFlag = PublishingFlag(mandatory = true)

  // Run when there's no consumer for the routing key specified by the publisher and the flag mandatory is true
  val publishingListener: PublishReturn => F[Unit] = pr => Sync[F].delay(println(s"Publish listener: $pr"))

  val program: F[Unit] = R.createConnectionChannel.use { implicit channel =>
    for {
      _ <- R.declareQueue(DeclarationQueueConfig.default(queueName))
      _ <- R.declareExchange(exchangeName, ExchangeType.Topic)
      _ <- R.bindQueue(queueName, exchangeName, routingKey)
      publisher <- R
        .createPublisherWithListener[AmqpMessage[String]](exchangeName, routingKey, publishingFlag, publishingListener)
      ackerConsumer <- R.createAckerConsumer[String](queueName)
      result = new Flow[F, String](ackerConsumer._2, ackerConsumer._1, logPipe, publisher).flow
      _ <- result.compile.drain
    } yield ()
  }
}
