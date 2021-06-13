package integration.jdbc.setting

import integration.PostgreSqlJsonType
import integration.setting.PostgreSqlSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class PostgreSqlJdbcSetting(url: String, user: String, password: String) : PostgreSqlSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, user, password, listOf(PostgreSqlJsonType())) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
