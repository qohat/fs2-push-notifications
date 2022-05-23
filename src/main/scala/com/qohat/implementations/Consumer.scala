package com.qohat.implementations

import cats.effect._
import com.qohat.model.Codecs._
import dev.profunktor.fs2rabbit.model.AckResult.{ Ack, NAck }
import dev.profunktor.fs2rabbit.model.{ AckResult, AmqpEnvelope }
import fs2.{ Pipe, Stream }
import io.circe.Error
import org.typelevel.log4cats.Logger

trait Consumer[F[_]] {
  def consume: Stream[F, Unit]
}

object ConsumerImpl {
  def impl[F[_]: Sync](
      consumer: Stream[F, AmqpEnvelope[String]],
      acker: AckResult => F[Unit],
      notificationClient: PushNotificationClient[F]
  )(implicit logger: Logger[F]): Consumer[F] =
    new Consumer[F] {

      val errorPipe: Pipe[F, Error, Unit] = _.evalMap(err => logger.error(err)(s"Failed to act on record"))

      override def consume: Stream[F, Unit] =
        consumer
          .evalTap(msg => logger.info(s"Consumed: $msg"))
          .map(notificationDecoder)
          .flatMap {
            case (Right(notification), tag) =>
              Stream(notification)
                .covary[F]
                .flatMap(notificationClient.push)
                .as(Ack(tag))
            case (Left(e), tag) =>
              Stream(e)
                .covary[F]
                .through(errorPipe)
                .as(NAck(tag))
          }
          .evalMap(acker)
    }
}
