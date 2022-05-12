package com.qohat.config

import dev.profunktor.fs2rabbit.config.Fs2RabbitNodeConfig
import cats.data.NonEmptyList

object Config:
  val config = Fs2RabbitNodeConfig(
    virtualHost = "/",
    nodes = NonEmptyList.one(
      Fs2RabbitNodeConfig(
        host = "127.0.0.1",
        port = 5672
      )
    ),
    username = Some("guest"),
    password = Some("guest"),
    ssl = false,
    connectionTimeout = 3,
    requeueOnNack = false,
    internalQueueSize = Some(500),
    automaticRecovery = true
  )
