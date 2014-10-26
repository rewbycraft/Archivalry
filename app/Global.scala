import com.roundeights.hasher.Implicits._
import models.db.{User, Users}
import play.api._
import play.api.db.slick.Config.driver.simple._

/**
 * Created by roelf on 10/25/14.
 */
object Global extends GlobalSettings {
	override def onStart(app: Application): Unit =
	{
		super.onStart(app)
		Logger.info("Checking for admin...")
		implicit val current = app
		play.api.db.slick.DB.withSession
		{
			implicit session =>
				val users = TableQuery[Users]
				val q = users.filter(u => u.username === "admin")
				if (q.size.run == 0)
				{
					Logger.info("No admin user found. Creating one...")
					users.insert(User("admin", "password".sha512.hex, admin = true)).run
				}
				else
				{
					val auser = q.list(session)(0)
					if (!auser.admin)
					{
						Logger.info("Admin user doesn't have admin perms. Wat?! FIXING NOW!")
						q.update(User(auser.username, auser.password, admin = true)).run
					}
					else
						Logger.info("Admin found.")
				}
		}
	}
}
