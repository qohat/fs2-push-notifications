package com.qohat.interpreter

import cats.effect.Async
import fs2.Stream
import org.typelevel.log4cats.Logger

class Flow[F[_]: Async](
    consumer: Consumer[F],
    publisher: Publisher[F]
)(implicit logger: Logger[F]) {
  val flow: Stream[F, Unit] =
    Stream(
      publisher.publish,
      consumer.consume
    ).parJoin(3)
}
