package authentication

import play.api.mvc._
import sun.misc.BASE64Decoder
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 * Basic http authentication
 *
 * Created by roelf on 10/20/14.
 */
object BasicAuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {


	override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] =
	{
		val authHeader = request.headers.get("authorization")
		if (!authHeader.isDefined)
			return Future { Results.Unauthorized("Unauthorized.\n").withHeaders(("WWW-Authenticate", "Basic realm=\"Archivalry\"")) }
		val credString = new String(new BASE64Decoder().decodeBuffer(authHeader.get.substring(6)), "UTF-8").split(":")
		if ((credString == null) || (credString.size != 2))
			return Future { Results.Unauthorized("Unauthorized.\n") }
		val username = credString(0)
		val password = credString(1)
		val user = UserManager.authUser(username, password)

		if (!user.isDefined)
			return Future { Results.Unauthorized("Unauthorized.\n") }

		block(AuthenticatedRequest[A](user.get, request))
	}
}
