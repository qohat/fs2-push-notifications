package com.qohat

import cats.effect._
import com.qohat.client.Rabbit
import com.qohat.config.Config
import com.qohat.programs.Program
import dev.profunktor.fs2rabbit.resiliency.ResilientStream

import scala.concurrent.duration.DurationInt

object IOAckerConsumer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Rabbit.create[IO](Config.config).use { client =>
      ResilientStream
        .runF(new Program[IO](client).program, 2.seconds)
        .as(ExitCode.Success)
    }

}
