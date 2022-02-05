package integration.jdbc

import integration.core.Setting
import org.komapper.jdbc.JdbcDatabaseConfig

object SettingProvider {

    fun get(): Setting<JdbcDatabaseConfig> {
        val driver = System.getProperty("driver") ?: error("The driver property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        val className = when (driver) {
            "h2" -> "integration.jdbc.h2.JdbcH2Setting"
            "mariadb" -> "integration.jdbc.mariadb.JdbcMariaDbSetting"
            "mysql" -> "integration.jdbc.mysql.JdbcMySqlSetting"
            "oracle" -> "integration.jdbc.oracle.JdbcOracleSetting"
            "postgresql" -> "integration.jdbc.postgresql.JdbcPostgreSqlSetting"
            "sqlserver" -> "integration.jdbc.sqlserver.JdbcSqlServerSetting"
            else -> error("Unsupported driver: $driver")
        }
        val clazz = Class.forName(className) ?: error("Invalid className: $className")
        val constructor = clazz.getDeclaredConstructor(String::class.java)
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(url) as Setting<JdbcDatabaseConfig>
    }
}
