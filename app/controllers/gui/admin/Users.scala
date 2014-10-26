package controllers.gui.admin

import authentication.{GUIAuthenticatedAdminAction, UserManager}
import models.forms.admin.users.AddUserForm
import play.api.mvc._

/**
 * Created by roelf on 10/25/14.
 */
class Users extends Controller {

	def addUser = GUIAuthenticatedAdminAction
	{
		implicit request =>
			Ok(views.html.admin.users.adduser(request))
	}

	def handleAddUser = GUIAuthenticatedAdminAction
	{
		implicit request =>
			import play.api.db.slick.Config.driver.simple._
			import com.roundeights.hasher.Implicits._
			AddUserForm.form.bindFromRequest().fold(
				hasErrors => BadRequest("Bad form.\n"),
				form =>
				{
					import play.api.Play.current
					play.api.db.slick.DB.withSession
					{
						implicit session =>
							UserManager.users.insert(models.db.User(form.username, form.password.sha512.hex, form.admin)).run
					}
					Redirect("/admin/users/list")
				}
			)
	}

	def profile = GUIAuthenticatedAdminAction
	{
		implicit request =>
			if (request.getQueryString("user").isDefined)
			{
				val user = UserManager.getUserFromUserName(request.getQueryString("user").get)
				if (user.isDefined)
					Ok(views.html.admin.users.profile(request, user.get))
				else
					BadRequest("Need a valid user.\n")
			}
			else
				Ok(views.html.admin.users.profile(request, request.user))
	}

	def list = GUIAuthenticatedAdminAction
	{
		implicit request =>
			Ok(views.html.admin.users.list(request))
	}

	def handleProfileChange = GUIAuthenticatedAdminAction
	{
		implicit request =>
			def f: Result =
			{
				import play.api.db.slick.Config.driver.simple._
				if (!request.getQueryString("user").isDefined)
					BadRequest("Need to get an user.\n")
				else if (!request.getQueryString("action").isDefined)
					BadRequest("Need to get an action.\n")
				else if (!UserManager.getUserFromUserName(request.getQueryString("user").get).isDefined)
					BadRequest("Need a valid user.\n")
				else
				{
					val user = UserManager.getUserFromUserName(request.getQueryString("user").get).get
					request.getQueryString("action").get match
					{
						case "changepassword" =>
							import com.roundeights.hasher.Implicits._
							val newPassword = request.body.asFormUrlEncoded.get("password")(0).sha512.hex
							import play.api.Play.current
							play.api.db.slick.DB.withSession
							{
								implicit session =>
									val q = UserManager.users.filter(u => u.username === user.username).filter(u => u.password === user.password).filter(u => u.admin === user.admin)
									q.update(models.db.User(user.username, newPassword, user.admin)).run
									UserManager.invalidateUserTokens(user.username)
							}
						case "deadmin" =>
							import play.api.Play.current
							play.api.db.slick.DB.withSession
							{
								implicit session =>
									val q = UserManager.users.filter(u => u.username === user.username).filter(u => u.password === user.password).filter(u => u.admin === user.admin)
									q.update(models.db.User(user.username, user.password, admin = false)).run
							}
						case "admin" =>
							import play.api.Play.current
							play.api.db.slick.DB.withSession
							{
								implicit session =>
									val q = UserManager.users.filter(u => u.username === user.username).filter(u => u.password === user.password).filter(u => u.admin === user.admin)
									q.update(models.db.User(user.username, user.password, admin = true)).run
							}
						case "delete" =>
							import play.api.Play.current
							play.api.db.slick.DB.withSession
							{
								implicit session =>
									val q = UserManager.users.filter(u => u.username === user.username).filter(u => u.password === user.password).filter(u => u.admin === user.admin)
									q.delete.run
									UserManager.invalidateUserTokens(user.username)
									return Redirect("/admin/users/list")
							}

					}
					Redirect("/admin/users/profile?user=" + request.getQueryString("user").get)
				}
			}
			f
	}
}
