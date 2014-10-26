package models.forms.admin.maven

import play.api.data._
import play.api.data.Forms._

/**
 * Created by roelf on 10/24/14.
 */
case class addrepoForm(repositoryname: String)

object addrepoForm {
	val form = Form(mapping("repositoryname" -> text)(addrepoForm.apply)(addrepoForm.unapply))
}
