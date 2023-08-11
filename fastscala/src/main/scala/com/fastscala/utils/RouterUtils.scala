package com.fastscala.utils

import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}
import java.io.ByteArrayOutputStream
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}
import scala.annotation.tailrec

object RouterUtils {


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

  var version = System.currentTimeMillis()
}
