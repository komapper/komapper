package integration.setting

import org.komapper.core.DatabaseConfig
import java.util.regex.Pattern

interface Setting {
    val config: DatabaseConfig
    val dbms: Dbms
    val createSql: String
    val dropSql: String
    val resetSql: String?

    companion object {
        private val jdbcUrlPattern = Pattern.compile("^jdbc:([^:]*):.*")

        fun get(): Setting {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val user = System.getProperty("user") ?: error("The user property is not found.")
            val password = System.getProperty("password") ?: error("The password property is not found.")
            return when (val subprotocol = extractSubprotocol(url)) {
                "h2" -> H2Setting(url, user, password)
                "mysql" -> MySqlSetting(url, user, password)
                "postgresql" -> PostgreSqlSetting(url, user, password)
                else -> error("Unsupported JDBC URL: url=$url, extractedSubprotocol=$subprotocol")
            }
        }

        private fun extractSubprotocol(url: String): String? {
            val matcher = jdbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).lowercase()
            }
            return null
        }
    }
}
