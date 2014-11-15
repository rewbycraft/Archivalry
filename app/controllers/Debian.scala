package controllers

import java.io._
import java.nio.charset.Charset

import akka.actor._
import authentication.BasicAuthenticatedAction
import models.db._
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.{GzipCompressorOutputStream, GzipCompressorInputStream}
import play.api.Logger
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._

/**
 *
 * repo
 * |-> distribution
 * |   |-> component
 * |   |   |-> arch
 * |   |   |   | -> package
 *
 * Created by roelf on 11/1/14.
 */
object DebianRepositoryManager {
	val binaryPackages = TableQuery[DebianBinaryPackages]
	val repositories = TableQuery[DebianRepositories]

	object Repositories {
		def add(name: String): Option[DebianRepository] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					if (repositories.filter(r => r.name.toLowerCase === name.toLowerCase).size.run > 0)
						return None //Duplicate detection
					repositories.insert(DebianRepository(0, name)).run
					Some(repositories.filter(r => r.name.toLowerCase === name.toLowerCase).list(session)(0))
			}
		}

		def getRepos: Array[DebianRepository] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					repositories.list.toArray
			}
		}

		def get(name: String): Option[DebianRepository] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					val l = repositories.filter(r => r.name.toLowerCase === name.toLowerCase).list
					if (l.size == 0)
						None
					else
						Some(l(0))
			}
		}

		def get(id: Int): Option[DebianRepository] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					val l = repositories.filter(r => r.repoId === id).list
					if (l.size == 0)
						None
					else
						Some(l(0))
			}
		}

		def del(name: String): Unit = del(get(name).get.repoId)

		def del(id: Int): Unit =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					if (!get(id).isDefined)
						return //Not here.
					binaryPackages.filter(d => d.repoId === id).foreach(p => Packages.del(p.fileId))
					repositories.filter(r => r.repoId === id).delete.run
			}
		}
	}

	object Packages {
		def add(uuid: String, repo: Int, controlFile: String): Option[DebianBinaryPackage] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					if (!Repositories.get(repo).isDefined)
						return None
					val i = DebianBinaryPackage(repo, uuid, controlFile)
					binaryPackages.insert(i).run
					Some(i)
			}
		}

		def get(uuid: String): Option[DebianBinaryPackage] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					val l = binaryPackages.filter(r => r.fileId.toLowerCase === uuid.toLowerCase).list
					if (l.size == 0)
						None
					else
						Some(l(0))
			}
		}

		def get(repo: Int): Option[Array[DebianBinaryPackage]] =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					val l = binaryPackages.filter(r => r.repoId === repo).list
					if (l.size == 0)
						None
					else
						Some(l.toArray)
			}
		}

		def del(uuid: String): Unit =
		{
			import play.api.Play.current
			play.api.db.slick.DB.withSession
			{
				implicit session =>
					binaryPackages.filter(p => p.fileId === uuid).delete.run
			}
		}
	}

}

object DebianUploadActor {
	val actorRef = play.libs.Akka.system().actorOf(Props[DebianUploadActor])

	case class DoFile(uuid: String, repo: String)

}

class DebianUploadActor extends Actor {
	val logger = Logger.logger

	def receive =
	{
		case DebianUploadActor.DoFile(fileid: String, repoName: String) =>
			def f: Unit =
			{
				logger.info("Processing debian upload: " + fileid)
				val fileinfo = FileManager.openFile(fileid).get
				val file = fileinfo._1
				val controlFile = DebianUtils.getControlFile(file)


				val repo = DebianRepositoryManager.Repositories.get(repoName)
				if (!repo.isDefined)
				{
					logger.error("Could not find repository: " + repoName)
					file.delete()
					return
				}

				DebianRepositoryManager.Packages.add(fileid, repo.get.repoId, controlFile)
			}
			f
	}
}

class Debian extends Controller {

	def upload(repo: String, filename: String) = BasicAuthenticatedAction(parse.raw(256 * 1024 * 1024))
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
				DebianUploadActor.actorRef ! DebianUploadActor.DoFile(t._2, repo)
				Ok("OK\n")
			}
			f
	}

	def file(repo: String, id: String) = Action
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


	def packages(repoName: String) = Action {
		implicit request =>
			def f: Result = {
				val repo = DebianRepositoryManager.Repositories.get(repoName)
				if (!repo.isDefined)
					return NotFound("Not found.")
				var returnValue = ""
				DebianRepositoryManager.Packages.get(repo.get.repoId).get.foreach(p => {
					returnValue += "Filename: ./" + p.fileId + "\n"
					returnValue += p.controlFile
					returnValue += "\n\n"
				})
				val bos = new ByteArrayOutputStream
				val gzip = new GzipCompressorOutputStream(bos)
				gzip.write(returnValue.getBytes(Charset.forName("UTF-8")))
				gzip.close()
				val r = Ok(bos.toByteArray)
				bos.close()
				r
			}
			f
	}

}

object DebianUtils {
	def getControlFile(file: java.io.File): String =
	{
		def getControlArchive: Array[Byte] =
		{
			val archiveInputStream = new ArArchiveInputStream(new FileInputStream(file))
			while (true)
			{
				val entry = archiveInputStream.getNextEntry
				if (entry == null)
					new RuntimeException("Invalid deb.")
				if (entry.getName == "control.tar.gz")
				{
					val content = new Array[Byte](entry.getSize.toInt)
					var read = 0
					while (read < entry.getSize)
						read += archiveInputStream.read(content, read, content.length - read)
					return content
				}
			}
			new RuntimeException("Invalid deb.")
			Array()
		}
		val archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new ByteArrayInputStream(getControlArchive)))
		while (true)
		{
			val entry = archiveInputStream.getNextEntry
			if (entry == null)
				new RuntimeException("Invalid deb.")
			if (entry.getName == "./control")
			{
				val content = new Array[Byte](entry.getSize.toInt)
				var read = 0
				while (read < entry.getSize)
					read += archiveInputStream.read(content, read, content.length - read)
				return new String(content)
			}
		}
		new RuntimeException("Invalid deb.")
		""
	}
}
