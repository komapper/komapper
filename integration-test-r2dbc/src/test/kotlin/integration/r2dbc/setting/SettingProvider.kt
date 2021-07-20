package integration.r2dbc.setting

import integration.setting.Setting
import org.komapper.r2dbc.R2dbcDatabaseConfig

object SettingProvider {

    fun get(): Setting<R2dbcDatabaseConfig> {
        return when (val driver = System.getProperty("driver") ?: error("The driver property is not found.")) {
            "h2" -> H2R2dbcSetting()
            "mariadb" -> MariaDbR2dbcSetting()
            "mysql" -> MySqlR2dbcSetting()
            "postgresql" -> PostgreSqlR2dbcSetting()
            else -> error("Unsupported driver: $driver")
        }
    }
}
