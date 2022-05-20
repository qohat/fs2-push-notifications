package com.qohat.programs

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import com.qohat.interpreter.{ ConsumerImpl, Flow, PublisherImpl, PushNotification }
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.charset.StandardCharsets.UTF_8

class Program[F[_]: Async](R: RabbitClient[F]) {

  private val queueName    = QueueName("testQ")
  private val exchangeName = ExchangeName("testEX")
  private val routingKey   = RoutingKey("testRK")
  implicit val stringMessageEncoder =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(UTF_8)).pure[F])

  implicit val logger: Logger[F]     = Slf4jLogger.getLogger[F]
  val publishingFlag: PublishingFlag = PublishingFlag(mandatory = true)

  // Run when there's no consumer for the routing key specified by the publisher and the flag mandatory is true
  val publishingListener: PublishReturn => F[Unit] = pr => Sync[F].delay(println(s"Publish listener: $pr"))

  val program: F[Unit] = R.createConnectionChannel.use { implicit channel =>
    for {
      _ <- R.declareQueue(DeclarationQueueConfig.default(queueName))
      _ <- R.declareExchange(exchangeName, ExchangeType.Topic)
      _ <- R.bindQueue(queueName, exchangeName, routingKey)
      pub <- R
        .createPublisherWithListener[AmqpMessage[String]](exchangeName, routingKey, publishingFlag, publishingListener)
      ackerConsumer          <- R.createAckerConsumer[String](queueName)
      pushNotificationClient <- PushNotification.create[F]
      consumer               <- ConsumerImpl.create[F](ackerConsumer._2, ackerConsumer._1, pushNotificationClient)
      publisher              <- PublisherImpl.create[F](pub)
      result = new Flow[F](consumer, publisher).flow
      _ <- result.compile.drain
    } yield ()
  }
}
