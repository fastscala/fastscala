package com.fastscala.scala_xml.xml.streaming

/**
 * What [[Encoder]] writes into: a byte buffer, filled from `pos` on, that's emptied when it's full.
 */
trait EncoderOutput {

  /** The buffer being filled. (Another one after a [[flush]], maybe: of the same length.) */
  def buf: Array[Byte]

  /** How many bytes of `buf` are filled: where the next one goes. */
  def pos: Int

  def pos_=(p: Int): Unit

  /** Takes what's in the buffer (if anything): after it, `buf` is empty (`pos` is 0). */
  def flush(): Unit
}
