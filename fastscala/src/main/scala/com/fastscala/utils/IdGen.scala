package com.fastscala.utils

import java.security.SecureRandom
import java.util.Base64
import scala.util.Random

object IdGen {

  private val rand = new Random()
  private val randGen = new SecureRandom()

  def id(suffix: String) = "id" + math.abs(rand.nextLong()).toHexString + "-" + suffix

  def id = "id" + math.abs(rand.nextLong()).toHexString

  def secureId(nBytes: Int = 32): String = {
    val bytes = Array.fill[Byte](32)(0)
    randGen.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes).replace("/", "_").replace("+", "-")
  }
}
