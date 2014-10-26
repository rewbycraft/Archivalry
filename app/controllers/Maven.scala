package controllers

import java.io.FileOutputStream

import authentication.BasicAuthenticatedAction
import models.db.{MavenArtifact, MavenArtifacts, MavenRepositories, MavenRepository}
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._

/**
 * Created by roelf on 10/17/14.
 */

object MavenRepositoryManager {
	val repos = TableQuery[MavenRepositories]
	val artifacts = TableQuery[MavenArtifacts]

	def getRepo(name: String): Option[MavenRepository] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val l = repos.filter(f => f.name.toLowerCase === name.toLowerCase).list
				if (l.size > 0)
					return Some(l(0))
				None
		}
	}

	def getArtifacts(id: Int): Array[models.db.MavenArtifact] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				artifacts.filter(a => a.repoId === id).list.toArray
		}
	}

	def getRepos: Array[MavenRepository] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				repos.list.toArray
		}
	}

	def addRepo(name: String): Boolean =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				if (repos.filter(r => r.name.toLowerCase === name.toLowerCase).size.run > 0)
					return false
				repos += MavenRepository(1, name)
				true
		}
	}

	def delRepo(id: Int): Boolean =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val q = repos.filter(r => r.id === id)
				if (q.size.run == 0)
					return false
				q.delete.run
				val q2 = artifacts.filter(a => a.repoId === id)
				q2.list.map(a => a.fileId).foreach(id => FileManager.deleteFile(id))
				q2.delete.run
				true
		}
	}
}

case class MavenArtifactData(group: String, artifact: String, version: String, filename: String)

object MavenArtifactData {
	def fromPath(path: String): MavenArtifactData =
	{
		val pathparts = path.split("/").filterNot(p => p.length == 0)
		val filepart = pathparts.reverse(0)
		val dirpart = pathparts.take(pathparts.length - 1)

		val version = dirpart.reverse(0)
		val artifact = dirpart.reverse(1)
		val group = dirpart.take(dirpart.length - 2).mkString(".")

		MavenArtifactData(group, artifact, version, filepart)
	}
}

class Maven extends Controller {
	def get(repoName: String, path: String) = Action
	{
		implicit request =>
			def f: Result =
			{
				val repo = MavenRepositoryManager.getRepo(repoName)
				if (!repo.isDefined)
					return NotFound("Repo not found.\n")

				val data = MavenArtifactData.fromPath(path)

				println(f"Received maven get for: $data on repo $repoName with request path $path")

				import play.api.Play.current
				play.api.db.slick.DB.withSession
				{
					implicit session =>
						val q = MavenRepositoryManager.artifacts
							.filter(a => a.repoId === repo.get.id)
							.filter(a => a.artifact === data.artifact)
							.filter(a => a.version === data.version)
							.filter(a => a.group === data.group)
							.filter(a => a.filename.toLowerCase === data.filename.toLowerCase)
							.list
						if (q.size > 0)
						{
							println("Redirecting to: " + q(0).fileId)
							TemporaryRedirect("/files/" + q(0).fileId)
						}
						else
						{
							println("Did not find a match.")
							NotFound("Not found.\n")
						}
				}
			}
			f
	}

	def put(repoName: String, path: String) = BasicAuthenticatedAction(parse.raw(256 * 1024 * 1024))
	{
		implicit request =>
			def f: Result =
			{
				val repo = MavenRepositoryManager.getRepo(repoName)
				if (!repo.isDefined)
					return NotFound("Repo not found.\n")

				val data = MavenArtifactData.fromPath(path)

				//Save file
				val raw = request.body.asBytes()
				if (!raw.isDefined)
					return BadRequest("No file uploaded.\n")
				val t = FileManager.createFile(data.filename)
				val fos = new FileOutputStream(t._1)
				fos.write(raw.get)
				fos.close()

				val af = MavenArtifact(repo.get.id, data.group, data.artifact, data.version, t._2, data.filename)

				import play.api.Play.current
				play.api.db.slick.DB.withSession
				{
					implicit session =>
						val q = MavenRepositoryManager.artifacts
							.filter(a => a.repoId === repo.get.id)
							.filter(a => a.artifact === data.artifact)
							.filter(a => a.version === data.version)
							.filter(a => a.group === data.group)
							.filter(a => a.filename.toLowerCase === data.filename.toLowerCase)
						if (q.size.run > 0)
						{
							//Replace existing
							val oldFile = q.list(session)(0).fileId
							FileManager.deleteFile(oldFile)
							q.update(af).run
						}
						else //add new
							MavenRepositoryManager.artifacts.insert(af).run
				}

				Ok("Ok\n")
			}
			f
	}
}
