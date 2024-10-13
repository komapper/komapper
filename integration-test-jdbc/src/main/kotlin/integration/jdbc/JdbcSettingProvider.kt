package integration.jdbc

import integration.core.Setting
import org.komapper.jdbc.JdbcDatabase

object JdbcSettingProvider {
    fun get(): Setting<JdbcDatabase> {
        val identifier = System.getProperty("identifier") ?: error("The identifier property is not found.")
        val url = System.getProperty("url") ?: error("The url property is not found.")
        val className = when (identifier) {
            "h2" -> "integration.jdbc.h2.JdbcH2Setting"
            "mariadb" -> "integration.jdbc.mariadb.JdbcMariaDbSetting"
            "mysql" -> "integration.jdbc.mysql.JdbcMySqlSetting"
            "mysql5" -> "integration.jdbc.mysql5.JdbcMySql5Setting"
            "oracle" -> "integration.jdbc.oracle.JdbcOracleSetting"
            "postgresql" -> "integration.jdbc.postgresql.JdbcPostgreSqlSetting"
            "sqlserver" -> "integration.jdbc.sqlserver.JdbcSqlServerSetting"
            else -> error("Unsupported database: $identifier")
        }
        val clazz = Class.forName(className) ?: error("Invalid className: $className")
        val constructor = clazz.getDeclaredConstructor(String::class.java)
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(url) as Setting<JdbcDatabase>
    }
}
