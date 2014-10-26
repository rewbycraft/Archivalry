package controllers.gui

import controllers.MavenRepositoryManager
import play.api.mvc._

/**
 * Created by roelf on 10/20/14.
 */
class Public extends Controller {
	def home = Action
	{
		implicit request =>
			Ok(views.html.public.home(request))
	}

	def mavenList = Action
	{
		implicit request =>
			Ok(views.html.public.maven(request))
	}

	def mavenInfo(name: String) = Action
	{
		implicit request =>
			val repo = MavenRepositoryManager.getRepo(name)
			if (!repo.isDefined)
				NotFound("Repo not found.\n")
			case class UniqueArtifact(group: String, artifact: String, version: String)
			val artifacts = MavenRepositoryManager.getArtifacts(repo.get.id)
				.map(a => UniqueArtifact(a.group, a.artifact, a.version))
				.distinct
				.map(a => (a.group, a.artifact, a.version))
			Ok(views.html.public.maveninfo(request, repo.get, artifacts))
	}

	def denied = Action
	{
		Forbidden("Access denied.\n")
	}
}
