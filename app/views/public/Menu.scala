package views.public

/**
 * Created by roelf on 10/26/14.
 */
object Menu {
	val menu = Array(
			MenuItem("Home", "/"),
			MenuItem("Maven Repositories", "/maveninfo")
	)
}

case class MenuItem(name: String, url: String)
