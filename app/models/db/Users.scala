package models.db

import play.api.db.slick.Config.driver.simple._

/**
 * Created by roelf on 10/20/14.
 */

case class User(username: String, password: String, admin: Boolean)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
	def username = column[String]("username", O.PrimaryKey, O.NotNull)
	def password = column[String]("password", O.NotNull)
	def admin = column[Boolean]("isadmin", O.NotNull)

	override def * = (username, password, admin) <> (User.tupled, User.unapply _)
}

case class UserToken(token: String, username: String, time: Long)

class UserTokens(tag: Tag) extends Table[UserToken](tag, "TOKENS") {
	def token = column[String]("token", O.PrimaryKey, O.NotNull)
	def username = column[String]("username", O.NotNull)
	def time = column[Long]("addedon", O.NotNull)

	override def * = (token, username, time) <> (UserToken.tupled, UserToken.unapply _)
}
