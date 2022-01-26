package integration.jdbc.setting

import integration.setting.Setting
import org.komapper.jdbc.JdbcDatabaseConfig

object SettingProvider {

    fun get(): Setting<JdbcDatabaseConfig> {
        val driver = System.getProperty("driver") ?: error("The driver property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        return when (driver) {
            "h2" -> H2JdbcSetting(url)
            "mariadb" -> MariaDbJdbcSetting(url)
            "mysql" -> MySqlJdbcSetting(url)
            "oracle" -> OracleJdbcSetting(url)
            "postgresql" -> PostgreSqlJdbcSetting(url)
            "sqlserver" -> SqlServerJdbcSetting(url)
            else -> error("Unsupported driver: $driver")
        }
    }
}
