package controllers

import models.db.DebianBinaryPackages
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._

import scala.collection.mutable.ArrayBuffer
import akka.actor._

/**
 * Created by roelf on 11/1/14.
 */


class DebianUploadActor extends Actor {
	val binaryPackages = TableQuery[DebianBinaryPackages]

	case class DoFile(uuid: String)

	def receive = {
		case DoFile(fileid) =>

	}
}

object DebianUploadActor {
	val actorRef = play.libs.Akka.system().actorOf(Props[DebianUploadActor])
}

class Debian extends Controller {

	val binaryPackages = TableQuery[DebianBinaryPackages]

	def getArchRelease(repo: String, distribution: String, component: String, arch: String): Option[String] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val l1 = binaryPackages.filter(p => p.repo === repo).filter(p => p.distribution === distribution).filter(p => p.component === component).filter(p => p.arch === arch)
				val l2 = binaryPackages.filter(p => p.repo === repo).filter(p => p.distribution === distribution).filter(p => p.component === component).filter(p => p.arch === "all")
				val l = l1 ++ l2
				if (l.size.run == 0)
					return None
				val rc = new ArrayBuffer[String]

				rc += "Archive: " + distribution
				rc += "Component: " + component
				rc += "Origin: Archivalry"
				rc += "Architecture: " + arch

				Some(rc.map(a => a + "\n").mkString)
		}
	}

	def getDistRelease(repo: String, distribution: String): Option[String] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val q = binaryPackages.filter(p => p.repo === repo).filter(p => p.distribution === distribution)
				if (q.size.run == 0)
					return None
				val rc = new ArrayBuffer[String]

				rc += "Suite: " + distribution
				rc += "Codename: " + distribution

				rc += "Components: " + q.map(p => p.component).list.distinct.mkString(" ")
				rc += "Origin: Archivalry"
				rc += "Architectures: " + q.map(p => p.arch).list.distinct.mkString(" ")

				Some(rc.map(a => a + "\n").mkString)
		}
	}

	def getArchPackages(repo: String, distribution: String, component: String, arch: String): Option[String] =
	{
		None
	}

	def archRelease(repo: String, distribution: String, _component: String, _binary: String) = Action
	{
		var component = _component
		var binary = _binary
		if (!binary.startsWith("binary-"))
		{
			component += "/" + binary
			binary = "binary-all"
		}
		val r = getArchRelease(repo, distribution, component, binary.replace("binary-", ""))
		if (r.isDefined)
			Ok(r.get)
		else
			NotFound("Not found.\n")
	}

	def distRelease(repo: String, distribution: String) = Action
	{
		val r = getDistRelease(repo, distribution)
		if (r.isDefined)
			Ok(r.get)
		else
			NotFound("Not found.\n")
	}
}
