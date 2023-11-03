package integration.r2dbc

import integration.core.Setting
import org.komapper.r2dbc.R2dbcDatabase

object R2dbcSettingProvider {

    fun get(): Setting<R2dbcDatabase> {
        val identifier = System.getProperty("identifier") ?: error("The identifier property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        val className = when (identifier) {
            "h2" -> "integration.r2dbc.h2.R2dbcH2Setting"
            "mariadb" -> "integration.r2dbc.mariadb.R2dbcMariaDbSetting"
            "mysql" -> "integration.r2dbc.mysql.R2dbcMySqlSetting"
            "mysql5" -> "integration.r2dbc.mysql5.R2dbcMySql5Setting"
            "oracle" -> "integration.r2dbc.oracle.R2dbcOracleSetting"
            "postgresql" -> "integration.r2dbc.postgresql.R2dbcPostgreSqlSetting"
            "sqlserver" -> "integration.r2dbc.sqlserver.R2dbcSqlServerSetting"
            else -> error("Unsupported database: $identifier")
        }
        val clazz = Class.forName(className) ?: error("Invalid className: $className")
        val constructor = clazz.getDeclaredConstructor(String::class.java)
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(url) as Setting<R2dbcDatabase>
    }
}
