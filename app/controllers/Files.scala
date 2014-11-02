package controllers

import java.io.FileOutputStream
import java.nio.file.Paths

import authentication.BasicAuthenticatedAction
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._

/**
 * Created by roelf on 10/17/14.
 */
object FileManager {

	val basePath = Paths.get("./filedb/")
	basePath.toFile.mkdirs()
	basePath.toFile.mkdir()

	val files = TableQuery[models.db.Files]

	def createFile(name: String): (java.io.File, String) =
	{
		val uuid = java.util.UUID.randomUUID().toString
		val file = new java.io.File(basePath.toAbsolutePath.toString, uuid)

		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				files += models.db.File(uuid, name)
		}

		file.createNewFile()

		(file, uuid)
	}

	def listFiles(): Array[models.db.File] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				files.list.toArray
		}
	}

	def deleteFile(id: String): Boolean =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val q = files.filter(f => f.id === id)
				if (q.list.size == 0)
					return false
				val affected = q.delete.run
				val file = new java.io.File(basePath.toAbsolutePath.toString, id)
				val fileres = file.delete()
				fileres
		}
	}

	def openFile(id: String, openReally: Boolean = true): Option[(java.io.File, String)] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val lf = files.filter(f => f.id === id).list
				if (lf.size == 0)
					return None
				if (!openReally)
					return Some((null, lf(0).filename))
				Some((new java.io.File(basePath.toAbsolutePath.toString, id), lf(0).filename))
		}
	}

}

class Files extends Controller {

	//For now, we ignore filenames
	def get(id: String, filename: String) = Action
	{
		implicit rs =>
			def f: Result =
			{
				val fl = FileManager.openFile(id)
				if (!fl.isDefined)
					return NotFound("Not found.\n")
				Ok.sendFile(content = fl.get._1, fileName = _ => fl.get._2)
			}
			f
	}

	def put(filename: String) = BasicAuthenticatedAction(parse.raw(memoryThreshold = 256 * 1024 * 1024))
	{
		implicit request =>
			def f: Result =
			{
				val raw = request.body.asBytes()
				if (!raw.isDefined)
					return BadRequest("No file uploaded.\n")
				val t = FileManager.createFile(filename)
				val fos = new FileOutputStream(t._1)
				fos.write(raw.get)
				fos.close()
				Ok(t._2 + "\n")
			}
			f
	}

	def delete(id: String) = BasicAuthenticatedAction
	{
		implicit request =>
			val a = FileManager.deleteFile(id)
			if (!a)
				NotFound("Not found/error.\n")
			else
				Ok("File deleted.\n")
	}
}
