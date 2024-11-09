package com.fastscala.core.circe

import io.circe.{Decoder, Json}
import com.fastscala.core.{FSContext, FSFunc}
import com.fastscala.js.Js

object CIrceSupport {

  implicit class FSContextWithCirceSupport(fsc: FSContext) {

    private def session = fsc.session

    def callbackJSON(
                      arg: Js,
                      func: Json => Js,
                      async: Boolean = true,
                      expectReturn: Boolean = true,
                      ignoreErrors: Boolean = false
                    ): Js = {
      fsc.callback(
        Js(if (arg.cmd.trim == "") "''" else s"JSON.stringify(${arg.cmd})"),
        (str: String) => {
          io.circe.parser.parse(str) match {
            case Left(value) => throw new Exception(s"Failed to parse JSON: ${value.getMessage()}")
            case Right(json) => func(json)
          }
        }, async, expectReturn, ignoreErrors)
    }

    def callbackJSONDecoded[A: Decoder](
                                         arg: Js,
                                         func: A => Js,
                                         async: Boolean = true,
                                         expectReturn: Boolean = true,
                                         ignoreErrors: Boolean = false
                                       ): Js = {
      fsc.callback(
        Js(if (arg.cmd.trim == "") "''" else s"JSON.stringify(${arg.cmd})"),
        (str: String) => {
          io.circe.parser.decode(str) match {
            case Left(value) => throw new Exception(s"Failed to parse JSON \"$str\": ${value.getMessage()}")
            case Right(decoded) => func(decoded)
          }
        }, async, expectReturn, ignoreErrors)
    }

  }
}
