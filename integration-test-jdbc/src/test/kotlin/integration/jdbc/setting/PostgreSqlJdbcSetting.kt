package integration.jdbc.setting

import integration.PostgreSqlJsonType
import integration.setting.PostgreSqlSetting
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.DefaultDatabaseConfig

class PostgreSqlJdbcSetting(url: String, user: String, password: String) : PostgreSqlSetting<DatabaseConfig> {
    override val config: DatabaseConfig =
        object : DefaultDatabaseConfig(url, user, password, listOf(PostgreSqlJsonType())) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
