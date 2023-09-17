package com.fastscala.utils

import com.fastscala.server.{ClientError, Ok, Response, RoutingHandlerHelper, RoutingHandlerNoSessionHelper}
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.io.IOUtils

import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}
import java.io.{ByteArrayOutputStream, InputStream}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Using
import scala.xml.{Elem, NodeSeq}

object FSOptimizedResourceHandler {

  def cssLoaderUrl(files: String*): String = "/static/optimized/css_loader.css?" + files.map("f=" + URLEncoder.encode(_, "UTF-8")).mkString("&")

  def cssLoaderElem(files: String*): Elem = <link rel="stylesheet" href={cssLoaderUrl(files: _*)} type="text/css"/>
}

class FSOptimizedResourceHandler(
                                  resourceRoots: Seq[String] = List("/web"),
                                  imagePostProcessHook: (String, BufferedImage) => BufferedImage = (_, bi) => bi,
                                  defaultMaxWidth: Int = 900,
                                  defaultMaxHeight: Int = 900,
                                  defaultCompression: Int = 7
                                ) extends RoutingHandlerNoSessionHelper {

  import RoutingHandlerHelper._

  private def openResource(name: String): Option[InputStream] =
    resourceRoots.iterator.map(root => Option(getClass.getResourceAsStream(root + name)))
      .find(_.isDefined).flatten

  private def rescale(scale: Double, bi: BufferedImage): BufferedImage = {
    val originalWidth = bi.getWidth
    val originalHeight = bi.getHeight
    val imgType = if (bi.getType == 0) BufferedImage.TYPE_INT_ARGB else bi.getType

    val resizedImage = new BufferedImage((originalWidth * scale).toInt, (originalHeight * scale).toInt, imgType)
    //    val g = resizedImage.createGraphics
    //    g.drawImage(bi, 0, 0, (originalWidth * scale).toInt, (originalHeight * scale).toInt, null)
    //    g.dispose()
    //    g.setComposite(AlphaComposite.Src)
    //    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    //    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    //    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val scaleTransform = AffineTransform.getScaleInstance(scale, scale)
    val bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR)
    bilinearScaleOp.filter(bi, resizedImage)
  }

  @tailrec private def resizeMaxWidth(maxWidth: Int, bi: BufferedImage): BufferedImage =
    if (maxWidth < bi.getWidth / 2) resizeMaxWidth(maxWidth, rescale(0.5, bi)) else bi

  @tailrec private def resizeMaxHeight(maxHeight: Int, bi: BufferedImage): BufferedImage =
    if (maxHeight < bi.getHeight / 2) resizeMaxHeight(maxHeight, rescale(0.5, bi)) else bi

  private def write(compression: Int, bi: BufferedImage): Array[Byte] = {
    val jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next

    val param = jpegWriter.getDefaultWriteParam
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    val quality = 0.1f * (10 - math.min(10, math.max(0, compression)))
    param.setCompressionQuality(quality)

    val out = new ByteArrayOutputStream()
    val memoryCacheImageOutputStream = new MemoryCacheImageOutputStream(out)
    jpegWriter.setOutput(memoryCacheImageOutputStream)
    jpegWriter.write(null, new IIOImage(bi, null, null), param)
    jpegWriter.dispose()
    memoryCacheImageOutputStream.close()

    out.toByteArray
  }

  private def write(bi: BufferedImage): Array[Byte] = {
    val jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next

    val param = jpegWriter.getDefaultWriteParam
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)

    val out = new ByteArrayOutputStream()
    val memoryCacheImageOutputStream = new MemoryCacheImageOutputStream(out)
    jpegWriter.setOutput(memoryCacheImageOutputStream)
    jpegWriter.write(null, new IIOImage(bi, null, null), param)
    jpegWriter.dispose()
    memoryCacheImageOutputStream.close()

    out.toByteArray
  }

  val cssLoaderCache = mutable.WeakHashMap[String, String]()

  override def handlerNoSession(implicit req: HttpServletRequest): Option[Response] = Some(req).collect({
    case Get("static", "optimized", "css_loader.css") =>
      val files = Option(req.getParameterValues("f")).getOrElse(Array()).reverse
      val css: String = cssLoaderCache.getOrElseUpdate(files.mkString(";"), {
        files.toList.map(java.net.URLDecoder.decode(_, "UTF-8")).reverse.map(file => {
          openResource(file) match {
            case Some(is) => Using(is)(is => IOUtils.toString(is, StandardCharsets.UTF_8)).get
            case None => s"/* File '$file': NOT FOUND*/" + "\n"
          }
        }).mkString("\n\n")
      })
      Ok.css(css)
        .addHeader("Cache-control", "public, max-age=7776000")

    case Req(GET, "static" :: "optimized" :: restOfPath, suf@("jpg" | "jpeg"), _) =>
      val remaining = java.net.URLDecoder.decode(restOfPath.mkString("", "/", "." + suf), "UTF-8")
      val resourceName = "/web/static/" + remaining
      val resource = getClass.getResource(resourceName)

      if (resource == null) {
        System.err.println(s"Not found: $resourceName")
        ClientError.NotFound
      } else if (remaining.toLowerCase.endsWith("jpg") || remaining.toLowerCase.endsWith("jpeg")) {
        val maxWidth: Option[Int] = Option(req.getParameter("max-width")).flatMap(_.toIntOption)
        val maxHeight: Option[Int] = Option(req.getParameter("max-height")).flatMap(_.toIntOption)
        val compression: Option[Int] = Option(req.getParameter("compression")).flatMap(_.toIntOption)
        val original: Boolean = Option(req.getParameter("original")).flatMap(_.toBooleanOption).getOrElse(false)
        val resource = getClass.getResource(resourceName)
        val is = resource.openStream()
        try {
          val byteArray: Array[Byte] = {
            if (original) {
              IOUtils.toByteArray(is)
            } else {
              val br: BufferedImage = ImageIO.read(is)
              val resizedWidth: BufferedImage = maxWidth.orElse(Some(defaultMaxWidth)).filter(_ > 0).map(maxWidth => {
                resizeMaxWidth(maxWidth, br)
              }).getOrElse(br)
              val resizedHeight: BufferedImage = maxHeight.orElse(Some(defaultMaxHeight)).filter(_ > 0).map(maxHeight => {
                resizeMaxHeight(maxHeight, resizedWidth)
              }).getOrElse(resizedWidth)
              val afterHook: BufferedImage = imagePostProcessHook("static/" + remaining, resizedHeight)
              write(compression.map(v => math.min(10, math.max(0, v))).getOrElse(defaultCompression), afterHook)
            }
          }
          Ok.binaryAutoDetectContentType(byteArray, restOfPath.last)
        } finally {
          is.close()
        }
      } else {
        val is = resource.openStream()
        try {
          Ok.binaryAutoDetectContentType(IOUtils.toByteArray(is), restOfPath.last)
            .addHeader("Cache-control", "public, max-age=7776000")
        } finally {
          is.close()
        }
      }
  })

  var version = System.currentTimeMillis()

  def optimizedCss(cssFiles: String*): NodeSeq = {
    <link rel="stylesheet" href={s"/static/css_loader.css?version=$version&" + cssFiles.map("f=" + _).mkString("&")} type="text/css" media="all"/>
  }
}
