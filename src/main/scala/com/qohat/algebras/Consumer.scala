package com.qohat.algebras

import fs2._

trait Consumer[F[_]] {
  def consume: Stream[F, Unit]
}
