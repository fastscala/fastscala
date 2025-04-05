package com.fastscala.components.aceeditor.json

import io.circe.Decoder
import io.circe.generic.semiauto


case class Position(row: Int, column: Int)

object Position {
  implicit val PositionDecoder: Decoder[Position] = semiauto.deriveDecoder[Position]
}