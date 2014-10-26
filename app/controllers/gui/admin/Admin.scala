package controllers.gui.admin

import authentication.GUIAuthenticatedAdminAction
import play.api.mvc._

/**
 * Created by roelf on 10/20/14.
 */
class Admin extends Controller {
	def home = GUIAuthenticatedAdminAction {
		implicit request =>
			Ok(views.html.admin.home(request))
	}


	def files = GUIAuthenticatedAdminAction {
		implicit request =>
			Ok(views.html.admin.files(request))
	}

}
