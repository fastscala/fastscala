package com.fastscala.xml.scala_xml

import com.fastscala.core.{FSXmlElem, FSXmlEnv, FSXmlNodeSeq, FSXmlSupport}
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq

import scala.xml.*

object FSScalaXmlEnv extends FSXmlEnv {
  override type NodeSeq = scala.xml.NodeSeq
  override type Elem = scala.xml.Elem

  implicit def env: FSXmlEnv = this
}

object FSScalaXmlSupport {

  implicit class RichElem(elem: scala.xml.Elem) {
    def asFSXml[E <: FSXmlEnv]()(using env: FSXmlSupport[E]): FSXmlElem[E] = env.buildElemFrom[FSScalaXmlEnv.type](elem)
  }

  implicit class RichNodeSeq(ns: scala.xml.NodeSeq) {
    def asFSXml[E <: FSXmlEnv]()(using env: FSXmlSupport[E]): FSXmlNodeSeq[E] = env.buildNodeSeqFrom[FSScalaXmlEnv.type](ns)
  }

  implicit def nodeSeq2FSXmlEnv[E <: FSXmlEnv](ns: scala.xml.NodeSeq)(using env: FSXmlSupport[E]): FSXmlNodeSeq[E] =
    env.buildUnparsed(ns.toString())

  implicit def elem2FSXmlEnv[E <: FSXmlEnv](elem: scala.xml.Elem)(using env: FSXmlSupport[E]): FSXmlElem[E] =
    env.buildElem(elem.label, elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" ")): _*)(elem.child.map(node => env.buildUnparsed(node.toString())): _*)

  implicit val fsXmlSupport: FSXmlSupport[FSScalaXmlEnv.type] = new FSXmlSupport[FSScalaXmlEnv.type] {

    override def render(elem: FSXmlNodeSeq[FSScalaXmlEnv.type]): String = elem.toString()

    override def buildElemFrom[E2 <: FSXmlEnv](other: FSXmlElem[E2])(using env: FSXmlSupport[E2]): FSXmlElem[FSScalaXmlEnv.type] = other match {
      case elem: scala.xml.Elem => elem
      case other => XML.loadString(env.render(env.elem2NodeSeq(other)))
    }

    override def buildNodeSeqFrom[E2 <: FSXmlEnv](other: FSXmlNodeSeq[E2])(using env: FSXmlSupport[E2]): FSXmlNodeSeq[FSScalaXmlEnv.type] = other match {
      case ns: scala.xml.NodeSeq => (ns: FSScalaXmlEnv.NodeSeq)
      case other => (XML.loadString(env.render(other)): FSScalaXmlEnv.NodeSeq)
    }

    override def buildElem(label: String, attrs: (String, String)*)(children: FSXmlNodeSeq[FSScalaXmlEnv.type]*): FSXmlElem[FSScalaXmlEnv.type] = {
      new ScalaXmlElemUtils.RichElem(new Elem(prefix = null, label = label, attributes1 = Null, scope = TopScope, minimizeEmpty = false, child = children.mkNS: _*)).withAttrs(attrs: _*)
    }

    override def label(elem: FSXmlElem[FSScalaXmlEnv.type]): String = elem.label

    override def getId(elem: FSXmlElem[FSScalaXmlEnv.type]): Option[String] = ScalaXmlElemUtils.RichElem(elem).getId

    override def contents(elem: FSXmlElem[FSScalaXmlEnv.type]): FSXmlNodeSeq[FSScalaXmlEnv.type] = elem.child

    override def attribute(elem: FSXmlElem[FSScalaXmlEnv.type], attrName: String): Option[String] = elem.attributes.get(attrName).map(_.mkString(""))

    override def attributes(elem: FSXmlElem[FSScalaXmlEnv.type]): List[(String, String)] = elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" "))

    override def transformAttribute(elem: FSXmlElem[FSScalaXmlEnv.type], attrName: String, transform: Option[String] => String): FSXmlElem[FSScalaXmlEnv.type] = attributeTransform(elem, attrName, transform)

    override def transformContents[E2 <: FSXmlEnv](elem: FSXmlElem[FSScalaXmlEnv.type], transform: FSXmlNodeSeq[FSScalaXmlEnv.type] => FSXmlNodeSeq[E2])(using env: FSXmlSupport[E2]): FSXmlElem[FSScalaXmlEnv.type] = transform(contents(elem)) match {
      case ns: scala.xml.NodeSeq => ScalaXmlElemUtils.RichElem(elem).apply(ns)
      case newContents =>
        val newContentsE2NodeSeq: FSXmlNodeSeq[E2] = newContents
        val newContentsENodeSeq: FSXmlNodeSeq[FSScalaXmlEnv.type] = buildNodeSeqFrom[E2](newContentsE2NodeSeq)(using env)
        val newContentsEScalaXmlNodeSeq: NodeSeq = newContentsENodeSeq
        ScalaXmlElemUtils.RichElem(elem).apply(newContentsEScalaXmlNodeSeq)
    }

    override def concat(ns1: FSXmlNodeSeq[FSScalaXmlEnv.type], ns2: FSXmlNodeSeq[FSScalaXmlEnv.type]): FSXmlNodeSeq[FSScalaXmlEnv.type] = ns1 ++ ns2

    override def elem2NodeSeq(elem: FSXmlElem[FSScalaXmlEnv.type]): FSXmlNodeSeq[FSScalaXmlEnv.type] = elem

    //*----------------------

    override def buildUnparsed(unparsed: String): NodeSeq = scala.xml.Unparsed(unparsed)

    override def buildText(text: String): NodeSeq = scala.xml.Text(text)

    override def Empty: NodeSeq = NodeSeq.Empty

    private def attributeTransform(elem: Elem, attrName: String, transform: Option[String] => String): Elem = {

      def updateMetaData(
                          metaData: MetaData = Option(elem.attributes).getOrElse(Null),
                          found: Boolean = false
                        ): MetaData = metaData match {
        case Null if !found => new UnprefixedAttribute(attrName, transform(None), Null)
        case Null if found => Null
        case PrefixedAttribute((pre, key, value, next)) if key == attrName =>
          new PrefixedAttribute(pre, key, value match {
            case null => Seq(Text(transform(None)))
            case Seq(Text(value)) => Seq(Text(transform(Some(value))))
            case other => other
          }, updateMetaData(next, found = true))
        case UnprefixedAttribute((key, value, next)) if key == attrName =>
          new UnprefixedAttribute(key, value match {
            case null => Seq(Text(transform(None)))
            case Seq(Text(value)) => Seq(Text(transform(Some(value))))
            case other => other
          }, updateMetaData(next, found = true))
        case PrefixedAttribute((pre, key, value, next)) => new PrefixedAttribute(pre, key, value, updateMetaData(next, found))
        case UnprefixedAttribute((key, value, next)) => new UnprefixedAttribute(key, value, updateMetaData(next, found))
      }

      new Elem(elem.prefix, elem.label, updateMetaData(), elem.scope, elem.minimizeEmpty, elem.child: _*)
    }

  }
}
