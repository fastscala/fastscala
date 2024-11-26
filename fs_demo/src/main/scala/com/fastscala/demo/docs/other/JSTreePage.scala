package com.fastscala.demo.docs.other

import com.fastscala.chartjs.*
import com.fastscala.core.FSContext
import com.fastscala.demo.components.{JSTree, JSTreeNode, JSTreeSimpleNode}
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, inScriptTag}

import scala.io.Source
import scala.xml.NodeSeq

class JSTreePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "JsTree Example"

  override def append2Head(): NodeSeq = super.append2Head() ++
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />

  override def append2Body(): NodeSeq = super.append2Body() ++
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {

      lazy val data = Source.fromResource("world-cities.csv").getLines().drop(1).map(_.split(",")).collect({
        case Array(name, country, subcountry, geonameid) => (country, subcountry, name)
      }).toVector
      lazy val country2Region2City: Map[String, Map[String, Vector[String]]] = data.groupBy(_._1).transform((k, v) => v.groupBy(_._2).transform((k, v) => v.map(_._3)))

      val jsTree = new JSTree[Unit] {
        override val rootNodes: Seq[JSTreeNode[Unit]] =
          List(new JSTreeSimpleNode[Unit]("Cities of the world", (), s"root")(
            country2Region2City.toVector.sortBy(_._1).map({
              case (country, region2City) =>
                new JSTreeSimpleNode[Unit](country, (), s"c_$country")(
                  region2City.toVector.sortBy(_._1).map({
                    case (region, cities) =>
                      new JSTreeSimpleNode[Unit](region, (), s"r_$region")(
                        cities.sorted.map(city =>
                          new JSTreeSimpleNode[Unit](city, (), s"c_$city", true)(Nil)
                        )
                      )
                  })
                )
            })
          ))
      }
      jsTree.render() ++ jsTree.init().onDOMContentLoaded.inScriptTag
    }
    closeSnippet()
  }
}
