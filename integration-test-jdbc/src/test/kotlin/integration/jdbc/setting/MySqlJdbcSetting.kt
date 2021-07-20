package integration.jdbc.setting

import integration.setting.MySqlSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class MySqlJdbcSetting(url: String) : MySqlSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, "test", "test") {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
