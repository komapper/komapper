package integration.jdbc.setting

import integration.setting.Setting
import org.komapper.jdbc.JdbcDatabaseConfig

object SettingProvider {

    fun get(): Setting<JdbcDatabaseConfig> {
        val driver = System.getProperty("driver") ?: error("The driver property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        val user = System.getProperty("user") ?: error("The user property is not found.")
        val password = System.getProperty("password") ?: error("The password property is not found.")
        return when (driver) {
            "h2" -> H2JdbcSetting(url, user, password)
            "mysql" -> MySqlJdbcSetting(url, user, password)
            "postgresql" -> PostgreSqlJdbcSetting(url, user, password)
            else -> error("Unsupported driver: $driver")
        }
    }
}
