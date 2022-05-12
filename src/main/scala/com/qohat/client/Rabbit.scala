package com.qohat.client

trait Rabbit[F[_]]:
  def get: Unit
