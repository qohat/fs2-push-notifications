package com.qohat.interpreter

import cats.effect.Async
import com.qohat.algebras.Consumer
import com.qohat.model.Codecs._
import com.qohat.model.Person
import dev.profunktor.fs2rabbit.model.AckResult.NAck
import dev.profunktor.fs2rabbit.model.{ AckResult, AmqpEnvelope, AmqpMessage }
import fs2.{ Pipe, Stream }
import io.circe.Error
import org.typelevel.log4cats.Logger

final case class ConsumerImpl[F[_]: Async](consumer: Stream[F, AmqpEnvelope[String]], acker: AckResult => F[Unit])(
    implicit logger: Logger[F]
) extends Consumer[F] {

  val personEncoderPipe: Pipe[F, AmqpMessage[Person], AmqpMessage[String]] = _.map(personJsonEncoder)
  val errorPipe: Pipe[F, Error, Unit] = _.evalMap(err => logger.error(err)(s"Failed to act on record"))

  override def consume: Stream[F, Unit] =
    consumer
      .evalTap(msg => logger.info(s"Consumed: $msg"))
      .map(personJsonDecoder)
      .flatMap {
        case (Right(person), tag) => Stream.empty
        case (Left(e), tag)       => Stream(e).covary[F].through(errorPipe).as(NAck(tag)).evalTap(acker)
      }
      .evalMap(acker)
}
