package com.fastscala.demo.docs.data

import scala.io.Source
import scala.util.Random

object Names {

  val FirstNames: List[String] = Source.fromInputStream(getClass.getResourceAsStream("/first_names.txt")).getLines().toList
  val LastNames: List[String] = Source.fromInputStream(getClass.getResourceAsStream("/last_names.txt")).getLines().toList

  val rand = new Random()

  def randomFirstName = FirstNames(rand.nextInt(FirstNames.size))

  def randomLastName = LastNames(rand.nextInt(LastNames.size))
}
