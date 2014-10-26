package models.forms

import play.api.data._
import play.api.data.Forms._

/**
 * Created by roelf on 10/22/14.
 */
case class AuthLoginForm(username: String, password: String)

object AuthLoginForm {
	val loginForm = Form(mapping("username" -> text, "password" -> text)(AuthLoginForm.apply)(AuthLoginForm.unapply))
}