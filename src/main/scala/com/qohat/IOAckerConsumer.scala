package com.qohat

import cats.data.NonEmptyList
import cats.effect._
import com.qohat.config.Config
import dev.profunktor.fs2rabbit.config.Fs2RabbitNodeConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.resiliency.ResilientStream

import scala.concurrent.duration.DurationInt

object IOAckerConsumer extends IOApp {

  val conf = new Config(
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

  /*val blockerResource =
    Resource
      .make(IO(Executors.newCachedThreadPool()))(es => IO(es.shutdown()))*/

  override def run(args: List[String]): IO[ExitCode] =
    RabbitClient.default[IO](conf).resource.use { client =>
      ResilientStream
        .runF(new AckerConsumerDemo[IO](client).program, 2.seconds)
        .as(ExitCode.Success)
    }

}
