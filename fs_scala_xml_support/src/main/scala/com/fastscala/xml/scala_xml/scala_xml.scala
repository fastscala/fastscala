package com.fastscala.xml.scala_xml

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.utils.{Renderable, RenderableWithFSContext}
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq

import scala.xml._

object FSScalaXmlEnv extends FSXmlEnv {
  override type NodeSeq = scala.xml.NodeSeq
  override type Elem = scala.xml.Elem

  implicit def env: FSXmlEnv = this
}

object FSScalaXmlSupport {

  //  implicit def toFSScalaXmlEnvNodeSeq[E <: FSXmlEnv : FSXmlSupport](ns: scala.xml.NodeSeq): E#NodeSeq = ns.asFSXml()

  //  implicit def toFSScalaXmlEnvElem[E <: FSXmlEnv : FSXmlSupport](ns: scala.xml.Elem): E#Elem = ns.asFSXml()

  implicit class RichElem(elem: scala.xml.Elem) {
    def asFSXml[E <: FSXmlEnv : FSXmlSupport](): E#Elem = implicitly[FSXmlSupport[E]].buildElemFrom[FSScalaXmlEnv.type](elem)
  }

  implicit class RichNodeSeq(ns: scala.xml.NodeSeq) {
    def asFSXml[E <: FSXmlEnv : FSXmlSupport](): E#NodeSeq = implicitly[FSXmlSupport[E]].buildNodeSeqFrom[FSScalaXmlEnv.type](ns)
  }

  implicit def nodeSeq2FSXmlEnv[E <: FSXmlEnv : FSXmlSupport](ns: scala.xml.NodeSeq): E#NodeSeq =
    implicitly[FSXmlSupport[E]].buildUnparsed(ns.toString())

  implicit def elem2FSXmlEnv[E <: FSXmlEnv : FSXmlSupport](elem: scala.xml.Elem): E#Elem =
    implicitly[FSXmlSupport[E]].buildElem(elem.label, elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" ")): _*)(elem.child.map(node => implicitly[FSXmlSupport[E]].buildUnparsed(node.toString())): _*)

  implicit val fsXmlSupport: FSXmlSupport[FSScalaXmlEnv.type] = new FSXmlSupport[FSScalaXmlEnv.type] {

    override def elem2NodeSeq(elem: Elem): NodeSeq = elem

    override def buildUnparsed(unparsed: String): NodeSeq = scala.xml.Unparsed(unparsed)

    override def concat(ns1: NodeSeq, ns2: NodeSeq): NodeSeq = ns1 ++ ns2

    override def buildText(text: String): NodeSeq = scala.xml.Text(text)

    override def buildElemFrom[E <: FSXmlEnv : FSXmlSupport](other: E#Elem): Elem = other match {
      case elem: scala.xml.Elem => elem
      case other => XML.loadString(implicitly[FSXmlSupport[E]].render(implicitly[FSXmlSupport[E]].elem2NodeSeq(other)))
    }

    override def buildNodeSeqFrom[E <: FSXmlEnv : FSXmlSupport](other: E#NodeSeq): NodeSeq = other match {
      case ns: scala.xml.NodeSeq => ns
      case other => XML.loadString(implicitly[FSXmlSupport[E]].render(other))
    }

    override def buildElem(label: String, attrs: (String, String)*)(children: NodeSeq*): Elem = {
      new ScalaXmlElemUtils.RichElem(new Elem(prefix = null, label = label, attributes1 = Null, scope = TopScope, minimizeEmpty = false, child = children.mkNS: _*)).withAttrs(attrs: _*)
    }

    override def label(elem: Elem): String = elem.label

    override def render(elem: NodeSeq): String = elem.toString()

    override def transformAttribute(elem: Elem, attrName: String, transform: Option[String] => String): Elem = attributeTransform(elem, attrName, transform)

    override def attribute(elem: Elem, attrName: String): Option[String] = elem.attributes.get(attrName).map(_.mkString(""))

    override def attributes(elem: Elem): List[(String, String)] = elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" "))

    override def contents(elem: Elem): NodeSeq = elem.child

    override def transformContents[E <: FSXmlEnv : FSXmlSupport](elem: Elem, transform: NodeSeq => E#NodeSeq): Elem = transform(contents(elem)) match {
      case ns: scala.xml.NodeSeq => ScalaXmlElemUtils.RichElem(elem).apply(ns)
      case newContents => ScalaXmlElemUtils.RichElem(elem).apply(buildNodeSeqFrom[E](newContents))
    }

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
