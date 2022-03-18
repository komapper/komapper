package integration.r2dbc

import integration.core.Setting
import org.komapper.r2dbc.R2dbcDatabase

object R2dbcSettingProvider {

    fun get(): Setting<R2dbcDatabase> {
        val driver = System.getProperty("driver") ?: error("The driver property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        val className = when (driver) {
            "h2" -> "integration.r2dbc.h2.R2dbcH2Setting"
            "mariadb" -> "integration.r2dbc.mariadb.R2dbcMariaDbSetting"
            "mysql" -> "integration.r2dbc.mysql.R2dbcMySqlSetting"
            "oracle" -> "integration.r2dbc.oracle.R2dbcOracleSetting"
            "postgresql" -> "integration.r2dbc.postgresql.R2dbcPostgreSqlSetting"
            "sqlserver" -> "integration.r2dbc.sqlserver.R2dbcSqlServerSetting"
            else -> error("Unsupported driver: $driver")
        }
        val clazz = Class.forName(className) ?: error("Invalid className: $className")
        val constructor = clazz.getDeclaredConstructor(String::class.java, String::class.java)
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(driver, url) as Setting<R2dbcDatabase>
    }
}
