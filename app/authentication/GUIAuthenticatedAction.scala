package authentication

import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Created by roelf on 10/20/14.
 */
object GUIAuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {


	override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] =
	{
		val authCookie = request.cookies.get("authtoken")

		if (!authCookie.isDefined)
			return Future
			{
				Results.Redirect("/auth/login/" + request.path)
			}

		val user = UserManager.getUserFromToken(authCookie.get.value)

		if (!user.isDefined)
			return Future
			{
				Results.Redirect("/auth/login/" + request.path)
			}

		block(AuthenticatedRequest[A](user.get, request))
	}
}
