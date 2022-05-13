package com.qohat.client

import cats.effect._
import dev.profunktor.fs2rabbit.interpreter.RabbitClient

object Rabbit {
  def apply[F[_]: Async]: F[RabbitClient[F]] = ???
}
