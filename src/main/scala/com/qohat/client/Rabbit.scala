package com.qohat.client

import cats.effect._
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient

object Rabbit {
  def apply[F[_]: Async](config: Fs2RabbitConfig): Resource[F, RabbitClient[F]] =
    RabbitClient.default[F](config).resource
}
