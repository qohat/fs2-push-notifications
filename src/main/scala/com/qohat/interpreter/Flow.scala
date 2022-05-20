package com.qohat.interpreter

import cats.effect.Async
import com.qohat.algebras.Consumer
import com.qohat.model.{ Address, Person }
import dev.profunktor.fs2rabbit.json.{ Fs2JsonDecoder, Fs2JsonEncoder }
import dev.profunktor.fs2rabbit.model.AckResult.NAck
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.{ LongVal, StringVal }
import dev.profunktor.fs2rabbit.model.{ AckResult, AmqpEnvelope, AmqpMessage, AmqpProperties }
import fs2.{ Pipe, Stream }
import io.circe.generic.auto._
import org.typelevel.log4cats.Logger
import io.circe._

class Flow[F[_]: Async](
    consumer: Consumer[F],
    acker: AckResult => F[Unit],
    publisher: AmqpMessage[String] => F[Unit]
)(implicit logger: Logger[F]) {

  val simpleMessage =
    AmqpMessage("Hey!", AmqpProperties(headers = Map("demoId" -> LongVal(123), "app" -> StringVal("fs2RabbitDemo"))))
  val classMessage = AmqpMessage(Person(1L, "Sherlock", Address(212, "Baker St")), AmqpProperties.empty)

  val flow: Stream[F, Unit] =
    Stream(
      Stream(simpleMessage).covary[F].evalMap(publisher),
      // Stream(classMessage).covary[F].through(personEncoderPipe).evalMap(publisher),
      consumer.consume
    ).parJoin(3)

}
