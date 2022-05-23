package com.qohat.implementations

import cats.effect._
import com.qohat.config.PushNotificationConfig
import com.qohat.model.Notification
import fs2._
import org.typelevel.log4cats.Logger

trait PushNotificationClient[F[_]] {
  def push(n: Notification): Stream[F, Unit]
}

object PushNotification {
  def impl[F[_]](config: PushNotificationConfig)(implicit F: Sync[F], logger: Logger[F]): PushNotificationClient[F] =
    new PushNotificationClient[F] {
      override def push(n: Notification): Stream[F, Unit] =
        Stream(Sync[F].delay(n))
          .evalMap(n => logger.info(s"pushing $n in $config"))
    }
}
