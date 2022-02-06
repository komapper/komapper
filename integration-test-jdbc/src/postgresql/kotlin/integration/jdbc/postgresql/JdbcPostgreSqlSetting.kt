package integration.jdbc.postgresql

import integration.core.PostgreSqlSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

@Suppress("unused")
class JdbcPostgreSqlSetting(url: String) : PostgreSqlSetting<JdbcDatabaseConfig> {

    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, "test", "test", listOf(PostgreSqlJsonType())) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
