package controllers

import authentication.{GUIAuthenticatedAdminAction, UserManager}
import models.forms.AuthLoginForm
import play.api.mvc._

/**
 * Created by roelf on 10/21/14.
 */
class AuthenticationController extends Controller {
	def login(url: String) = Action
	{
		implicit request =>
			Ok(views.html.auth.login(url, AuthLoginForm.loginForm, null))
	}


	def handleLogin(url: String) = Action
	{
		implicit request =>
			AuthLoginForm.loginForm.bindFromRequest.fold(
				formWithErrors => BadRequest(views.html.auth.login(url, formWithErrors, "Invalid form")),
				form =>
				{
					val token = UserManager.authUserWithToken(form.username, form.password)
					if (!token.isDefined)
						Forbidden(views.html.auth.login(url, AuthLoginForm.loginForm, "Wrong password."))
					else
						Redirect(url).withCookies(new Cookie("authtoken", token.get.token))
				}
			)
	}

	def logout = GUIAuthenticatedAdminAction {
		implicit request =>
			UserManager.invalidateToken(request.cookies.get("authtoken").get.value)
			Redirect("/")
	}
}
