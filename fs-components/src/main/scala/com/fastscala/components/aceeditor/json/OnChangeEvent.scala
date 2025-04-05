package com.fastscala.components.aceeditor.json

import io.circe.Decoder
import io.circe.generic.semiauto

case class OnChangeEvent(
                          start: Position,
                          end: Position,
                          action: String,
                          lines: List[String],
                          id: Long
                        )

object OnChangeEvent {
  implicit val OnChangeEventDecoder: Decoder[OnChangeEvent] = semiauto.deriveDecoder[OnChangeEvent]
}