package com.qohat.model

import dev.profunktor.fs2rabbit.json.{ Fs2JsonDecoder, Fs2JsonEncoder }
import io.circe.generic.auto._

import java.util.UUID

case class Address(number: Int, streetName: String)
case class Person(id: Long, name: String, address: Address)

case class Notification(id: Id, message: Message)
case class Id(value: UUID)        extends AnyVal
case class Message(value: String) extends AnyVal

object Codecs {
  val encoder = new Fs2JsonEncoder
  val decoder = new Fs2JsonDecoder

  val personJsonEncoder = encoder.jsonEncode[Person]
  val personJsonDecoder = decoder.jsonDecode[Person]

  val notificationDecoder = decoder.jsonDecode[Notification]
  val notificationEncoder = encoder.jsonEncode[Notification]
}
