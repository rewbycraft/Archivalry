package models.forms.admin.users

import play.api.data._
import play.api.data.Forms._

/**
 * Created by roelf on 10/26/14.
 */
case class AddUserForm(username: String, password: String, admin: Boolean)

object AddUserForm {
	def form = Form(mapping("username" -> text, "password" -> text, "admin" -> boolean)(AddUserForm.apply)(AddUserForm.unapply))
}
