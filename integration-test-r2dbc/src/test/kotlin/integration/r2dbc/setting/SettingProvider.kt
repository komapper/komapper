package integration.r2dbc.setting

import integration.setting.Setting
import org.komapper.r2dbc.R2dbcDatabaseConfig

object SettingProvider {

    fun get(): Setting<R2dbcDatabaseConfig> {
        val driver = System.getProperty("driver") ?: error("The driver property is not found.")
        val database = System.getProperty("database") ?: error("The database property is not found.")
        val user = System.getProperty("user") ?: error("The user property is not found.")
        val password = System.getProperty("password") ?: error("The password property is not found.")
        return when (driver) {
            "h2" -> H2R2dbcSetting(driver)
            "mysql" -> MySqlR2dbcSetting(driver, database, user, password)
            "postgresql" -> PostgreSqlR2dbcSetting(driver, database, user, password)
            else -> error("Unsupported driver: $driver")
        }
    }
}
