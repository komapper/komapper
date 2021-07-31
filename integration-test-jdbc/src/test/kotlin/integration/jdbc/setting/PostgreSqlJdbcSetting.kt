package integration.jdbc.setting

import integration.jdbc.PostgreSqlJsonType
import integration.setting.PostgreSqlSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class PostgreSqlJdbcSetting(url: String) : PostgreSqlSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, "test", "test", listOf(PostgreSqlJsonType())) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
