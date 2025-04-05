package com.fastscala.components.aceeditor.json

import io.circe.Decoder
import io.circe.generic.semiauto

case class SelectionRange(
                          start: Position,
                          end: Position,
                        )

object SelectionRange {
  implicit val SelectionRangeDecoder: Decoder[SelectionRange] = semiauto.deriveDecoder[SelectionRange]
}