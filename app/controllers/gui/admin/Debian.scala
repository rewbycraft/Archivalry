package controllers.gui.admin

import authentication.GUIAuthenticatedAdminAction
import controllers.DebianRepositoryManager
import models.forms.admin.maven.addrepoForm
import play.api.mvc._

/**
 * Created by roelf on 11/2/14.
 */
class Debian extends Controller {
	def repos = Action
	{
		implicit request =>
			Ok(views.html.admin.debian.repos(request))
	}

	def addrepo = GUIAuthenticatedAdminAction
	{
		implicit request =>
			Ok(views.html.admin.debian.addrepo(request))
	}

	def handleAddrepo = GUIAuthenticatedAdminAction
	{
		implicit request =>
			addrepoForm.form.bindFromRequest.fold(
				hasError => BadRequest(views.html.admin.debian.addrepo(request)),
				form =>
				{
					DebianRepositoryManager.Repositories.add(form.repositoryname)
					Redirect("/admin/debian/repos")
				}
			)

	}
}
