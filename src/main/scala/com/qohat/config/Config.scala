package com.qohat.config
import cats.data.NonEmptyList
import dev.profunktor.fs2rabbit.config.{ Fs2RabbitConfig, Fs2RabbitNodeConfig }
import scala.concurrent.duration.{ DurationInt, FiniteDuration }

class Config(
    override val nodes: NonEmptyList[Fs2RabbitNodeConfig],
    override val virtualHost: String,
    override val connectionTimeout: FiniteDuration,
    override val ssl: Boolean,
    override val username: Option[String],
    override val password: Option[String],
    override val requeueOnNack: Boolean,
    override val requeueOnReject: Boolean,
    override val internalQueueSize: Option[Int],
    override val requestedHeartbeat: FiniteDuration,
    override val automaticRecovery: Boolean,
    override val clientProvidedConnectionName: Option[String]
) extends Fs2RabbitConfig(
      nodes,
      virtualHost,
      connectionTimeout,
      ssl,
      username,
      password,
      requeueOnNack,
      requeueOnReject,
      internalQueueSize,
      requestedHeartbeat,
      automaticRecovery,
      clientProvidedConnectionName
    )

object Config {
  val config = new Config(
    nodes = NonEmptyList.one(
      Fs2RabbitNodeConfig(
        host = "127.0.0.1",
        port = 5672
      )
    ),
    virtualHost = "/",
    connectionTimeout = 30.seconds,
    ssl = false,
    username = Some("guest"),
    password = Some("guest"),
    requeueOnNack = false,
    requeueOnReject = true,
    internalQueueSize = Some(0),
    requestedHeartbeat = 30.seconds,
    automaticRecovery = true,
    None
  )
}
