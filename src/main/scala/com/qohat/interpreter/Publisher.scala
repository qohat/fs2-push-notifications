package com.qohat.interpreter

import cats.effect.Async
import com.qohat.model.Codecs.{ notificationEncoder, personJsonEncoder }
import com.qohat.model._
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.{ LongVal, StringVal }
import dev.profunktor.fs2rabbit.model.{ AmqpMessage, AmqpProperties }
import fs2.{ Pipe, Stream }
import org.typelevel.log4cats.Logger

import java.util.UUID

trait Publisher[F[_]] {
  def publish: Stream[F, Unit]
}

final case class PublisherImpl[F[_]: Async](publisher: AmqpMessage[String] => F[Unit])(implicit
    logger: Logger[F]
) extends Publisher[F] {

  val personEncoderPipe: Pipe[F, AmqpMessage[Person], AmqpMessage[String]]             = _.map(personJsonEncoder)
  val notificationEncoderPipe: Pipe[F, AmqpMessage[Notification], AmqpMessage[String]] = _.map(notificationEncoder)

  val simpleMessage =
    AmqpMessage("Hey!", AmqpProperties(headers = Map("demoId" -> LongVal(123), "app" -> StringVal("fs2RabbitDemo"))))
  val classMessage = AmqpMessage(Person(1L, "Sherlock", Address(212, "Baker St")), AmqpProperties.empty)
  val notificationMessage =
    AmqpMessage(Notification(Id(UUID.randomUUID()), Message("This is a message")), AmqpProperties.empty)

  override def publish: Stream[F, Unit] =
    Stream(
      Stream(simpleMessage).covary[F].evalMap(publisher),
      Stream(classMessage).covary[F].through(personEncoderPipe).evalMap(publisher),
      Stream(notificationMessage).covary[F].through(notificationEncoderPipe).evalMap(publisher)
    ).parJoin(2)

}

object PublisherImpl {
  def create[F[_]: Async](publisher: AmqpMessage[String] => F[Unit])(implicit logger: Logger[F]): F[Publisher[F]] =
    Async[F].delay(PublisherImpl(publisher))
}
