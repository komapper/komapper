package integration.jdbc.setting

import integration.setting.MySqlSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class MySqlJdbcSetting(url: String, user: String, password: String) : MySqlSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, user, password) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
