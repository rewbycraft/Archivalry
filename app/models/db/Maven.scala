package models.db

import play.api.db.slick.Config.driver.simple._

/**
 * Created by roelf on 10/19/14.
 */
case class MavenRepository(id: Int, name: String)

case class MavenArtifact(repoId: Int, group: String, artifact: String, version: String, fileId: String, fileName: String)

class MavenRepositories(tag: Tag) extends Table[MavenRepository](tag, "MAVENREPOSITORIES") {

	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def name = column[String]("name", O.NotNull)

	override def * = (id, name) <> (MavenRepository.tupled, MavenRepository.unapply _)
}

class MavenArtifacts(tag: Tag) extends Table[MavenArtifact](tag, "MAVENARTIFACTS") {
	def repoId = column[Int]("repoid", O.NotNull)
	def group = column[String]("group", O.NotNull)
	def artifact = column[String]("artifact", O.NotNull)
	def version = column[String]("version", O.NotNull)
	def fileId = column[String]("fileid", O.NotNull)
	def filename = column[String]("filename", O.NotNull)

	override def * = (repoId, group, artifact, version, fileId, filename) <> (MavenArtifact.tupled, MavenArtifact.unapply _)
}

