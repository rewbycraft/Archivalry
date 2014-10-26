package views.admin

/**
 * Created by roelf on 10/24/14.
 */
object Menu {
	val menu = Array(
		MenuSection(Array(
			MenuItem("Overview", "/admin/"),
			MenuItem("File list", "/admin/files/list"),
			MenuItem("Maven Repositories", "/admin/maven/repos"),
			MenuItem("Users", "/admin/users/list")
		))
	)
}

case class MenuSection(items: Array[MenuItem])

case class MenuItem(name: String, url: String)
