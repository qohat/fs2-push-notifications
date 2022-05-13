package com.qohat.model

import dev.profunktor.fs2rabbit.json.Fs2JsonDecoder

import java.util.UUID

case class Notification(id: UUID, destination: Destination, message: Message)
case class Destination(value: String) extends AnyVal
case class Message(value: String)     extends AnyVal

object ioDecoder extends Fs2JsonDecoder
