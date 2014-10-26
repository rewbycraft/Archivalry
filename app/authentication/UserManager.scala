package authentication

import com.roundeights.hasher.Implicits._
import models.db.{User, UserToken, UserTokens, Users}
import play.api.db.slick.Config.driver.simple._
import play.api.mvc.Request

import scala.language.postfixOps

/**
 * Created by roelf on 10/20/14.
 */
object UserManager {

	val users = TableQuery[Users]
	val userTokens = TableQuery[UserTokens]

	def authUser(user: String, plainPassword: String): Option[User] =
	{
		val password = plainPassword.sha512.hex
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val l = users.filter(u => u.username === user).filter(u => u.password === password).list
				if (l.size == 0)
					return None
				Some(l(0))
		}
	}

	def authUserWithToken(username: String, password: String): Option[UserToken] =
	{
		cleanupTokens()
		val _user = authUser(username, password)
		if (!_user.isDefined)
			return None
		val user = _user.get
		val token = UserToken(java.util.UUID.randomUUID().toString, user.username, System.currentTimeMillis() / 1000L)
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				userTokens += token
		}
		Some(token)
	}

	def cleanupTokens(): Unit = {
		val currentT = System.currentTimeMillis() / 1000L - 30*60
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				userTokens.filter(t => t.time < currentT.toLong).delete.run
		}
	}

	def getUserFromToken(token: String): Option[User] =
	{
		cleanupTokens()
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val l = userTokens.filter(t => t.token.toLowerCase.trim === token.toLowerCase.trim).list
				if (l.size == 0)
					return None
				userTokens.filter(t => t.token.toLowerCase.trim === token.toLowerCase.trim).update(UserToken(l(0).token, l(0).username, System.currentTimeMillis() / 1000L)).run
				getUserFromUserName(l(0).username)
		}
	}

	def invalidateToken(token: String): Boolean = {
		cleanupTokens()
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				userTokens.filter(t => t.token.toLowerCase.trim === token.toLowerCase.trim).delete.run > 0
		}
	}

	def invalidateUserTokens(user: String): Boolean = {
		cleanupTokens()
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				userTokens.filter(t => t.username.toLowerCase === user.toLowerCase).delete.run > 0
		}
	}

	def getUsers: Array[User] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				users.list.toArray
		}
	}

	def getUserFromUserName(name: String): Option[User] =
	{
		import play.api.Play.current
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val l = users.filter(u => u.username === name).list
				if (l.size == 0)
					return None
				Some(l(0))
		}
	}
}

trait AuthenticatedRequest[+A] extends Request[A] {
	val user: User
}

object AuthenticatedRequest {
	def apply[A](u: User, r: Request[A]) = new AuthenticatedRequest[A] {
		def id = r.id

		def tags = r.tags

		def uri = r.uri

		def path = r.path

		def method = r.method

		def version = r.version

		def queryString = r.queryString

		def headers = r.headers

		lazy val remoteAddress = r.remoteAddress

		def username = None

		val body = r.body

		val user = u

		val secure: Boolean = r.secure
	}
}
