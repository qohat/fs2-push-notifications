package com.qohat.interpreter

import cats.effect.Sync
import com.qohat.model.Notification
import fs2._
import org.typelevel.log4cats.Logger

trait PushNotificationClient[F[_]] {
  def push(n: Notification): Stream[F, Unit]
}

final case class PushNotification[F[_]: Sync](implicit logger: Logger[F]) extends PushNotificationClient[F] {
  override def push(n: Notification): Stream[F, Unit] =
    Stream(n)
      .covary[F]
      .evalMap(n => logger.info(s"$n"))
}

object PushNotification {
  def create[F[_]: Sync](implicit logger: Logger[F]): F[PushNotificationClient[F]] =
    Sync[F].delay(PushNotification())
}
