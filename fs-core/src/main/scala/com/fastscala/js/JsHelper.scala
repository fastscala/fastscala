package com.fastscala.js

import java.security.SecureRandom


object JsHelper {

  val secureRandom = new SecureRandom()

  /** JavaScript operators on Js (with `js"..."`: the operands stay parts of the result). */
  implicit class RichJs(val js: Js) extends AnyVal {

    def `_==`(other: Js): Js = js"$js == $other"

    def `_!=`(other: Js): Js = js"$js != $other"

    def `_===`(other: Js): Js = js"$js === $other"

    def `_!==`(other: Js): Js = js"$js !== $other"

    def `_>`(other: Js): Js = js"$js > $other"

    def `_>=`(other: Js): Js = js"$js >= $other"

    def `_<`(other: Js): Js = js"$js < $other"

    def `_<=`(other: Js): Js = js"$js <= $other"

    def `_&&`(other: Js): Js = js"$js && $other"

    def `_||`(other: Js): Js = js"$js || $other"

    def `_=`(other: Js): Js = js"$js = $other;"

    def `_===null`: Js = js"$js === null"

    /** `code`, with `js`'s value in a new variable (with a random name), given to `code`. */
    def asVar(code: Js => Js): Js = {
      val name = "v_" + "%04X".formatted(secureRandom.nextInt())
      js"var $name = ($js); ${code(Js(name))}"
    }
  }

}
