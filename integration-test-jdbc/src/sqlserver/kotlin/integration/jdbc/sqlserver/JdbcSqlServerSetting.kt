package integration.jdbc.sqlserver

import integration.setting.SqlServerSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class JdbcSqlServerSetting(url: String) : SqlServerSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, "test", "test") {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
