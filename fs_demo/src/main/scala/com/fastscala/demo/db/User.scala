package com.fastscala.demo.db

import at.favre.lib.crypto.bcrypt.BCrypt
import com.fastscala.core.{FSContext, FSSessionVarOpt}
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS

import java.io.File
import java.nio.file.Files
import java.util.Base64
import scala.xml.Elem

object CurrentUser extends FSSessionVarOpt[User]()

class User(
            val firstName: String,
            val lastName: String,
            val username: String,
            var passwordHashed: String = "",
            var loginToken: String = IdGen.id,
            var photo: Option[(String, Array[Byte])] = None,
          ) {

  def fullName = firstName + " " + lastName

  def setPassword(password: String): this.type = {
    passwordHashed = BCrypt.withDefaults().hashToString(12, password.toCharArray())
    this
  }

  def checkPassword(password: String): Boolean = BCrypt.verifyer().verify(password.toCharArray(), passwordHashed).verified

  def logOut()(implicit fg: FSContext): Js = fg.callback(() => {
    loginToken = IdGen.id
    CurrentUser.clear()
    JS.deleteCookie("user_token", "/") & JS.redirectTo("/login")
  })

  def miniHeadshotOrPlaceholderRendered: Elem = {
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    photo.map({
      case (photoFileName, photoBytes) =>
        def imageData = s"data:${Files.probeContentType(new File(photoFileName).toPath())};base64,${Base64.getEncoder.encodeToString(photoBytes)}"

        div.withStyle(
          s"background-image: url('$imageData');background-size: cover;background-position: center; box-shadow: inset 0 0 1em 0em #00000029; aspect-ratio: 1;"
        ).d_inline_block
    }).getOrElse({
      <img src="/static/images/user-159-white.svg" class="card-img-top" style={s"background-color: #dddddd;"}/>
    })
  }

}