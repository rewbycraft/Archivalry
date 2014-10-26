package controllers.gui.admin

import authentication.GUIAuthenticatedAdminAction
import controllers.MavenRepositoryManager
import models.forms.admin.maven.addrepoForm
import play.api.mvc._

/**
 * Created by roelf on 10/24/14.
 */
class Maven extends Controller {
	def repos = GUIAuthenticatedAdminAction
	{
		implicit request =>
			Ok(views.html.admin.maven.repos(request))
	}

	def addrepo = GUIAuthenticatedAdminAction
	{
		implicit request =>
			Ok(views.html.admin.maven.addrepo(request))
	}

	def delrepo = GUIAuthenticatedAdminAction
	{
		implicit request =>
			def f: Result =
			{
				val sID = request.getQueryString("id")
				if (!sID.isDefined)
					return BadRequest("No id specified.\n")
				if (!(sID.get forall Character.isDigit))
					return BadRequest("Require numeric id.\n")
				val rc = MavenRepositoryManager.delRepo(sID.get.toInt)
				if (rc)
					Redirect("/admin/maven/repos")
				else
					NotFound("Could not find or delete repo.\n")
			}
			f
	}

	def handleAddrepo = GUIAuthenticatedAdminAction
	{
		implicit request =>
			addrepoForm.form.bindFromRequest.fold(
				hasError => BadRequest(views.html.admin.maven.addrepo(request)),
				form =>
				{
					MavenRepositoryManager.addRepo(form.repositoryname)
					Redirect("/admin/maven/repos")
				}
			)

	}
}
