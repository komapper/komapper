package integration.r2dbc.setting

import org.komapper.r2dbc.R2dbcDatabaseConfig
import java.util.regex.Pattern

interface Setting {
    val config: R2dbcDatabaseConfig
    val dbms: Dbms
    val createSql: String
    val dropSql: String
    val resetSql: String?

    companion object {
        private val urlPattern = Pattern.compile("^r2dbc:([^:]*):.*")

        fun get(): Setting {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val user = System.getProperty("user") ?: error("The user property is not found.")
            val password = System.getProperty("password") ?: error("The password property is not found.")
            return when (val driver = extractDriver(url)) {
                "h2" -> H2Setting(url, user, password)
                else -> error("Unsupported R2DBC URL: url=$url, driver=$driver")
            }
        }

        private fun extractDriver(url: String): String? {
            val matcher = urlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).lowercase()
            }
            return null
        }
    }
}
