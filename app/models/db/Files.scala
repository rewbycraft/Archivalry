package models.db

import play.api.db.slick.Config.driver.simple._

/**
 * Created by roelf on 10/17/14.
 */

case class File(id: String, filename: String)

class Files(tag: Tag) extends Table[File](tag, "FILES") {

	def id = column[String]("id", O.PrimaryKey)
	def filename = column[String]("filename", O.NotNull)

	override def * = (id, filename) <> (File.tupled, File.unapply _)
}
