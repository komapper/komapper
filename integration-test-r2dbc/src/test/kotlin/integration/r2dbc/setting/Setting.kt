package integration.r2dbc.setting

import org.komapper.r2dbc.R2dbcDatabaseConfig

interface Setting {
    val config: R2dbcDatabaseConfig
    val dbms: Dbms
    val createSql: String
    val dropSql: String
    val resetSql: String?

    companion object {

        fun get(): Setting {
            val driver = System.getProperty("driver") ?: error("The driver property is not found.")
            val database = System.getProperty("database") ?: error("The database property is not found.")
            val user = System.getProperty("user") ?: error("The user property is not found.")
            val password = System.getProperty("password") ?: error("The password property is not found.")
            return when (driver) {
                "h2" -> H2Setting(driver)
                "postgresql" -> PostgreSqlSetting(driver, database, user, password)
                else -> error("Unsupported driver: $driver")
            }
        }
    }
}
