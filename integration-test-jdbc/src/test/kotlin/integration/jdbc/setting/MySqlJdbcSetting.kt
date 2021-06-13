package integration.jdbc.setting

import integration.setting.MySqlSetting
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.DefaultDatabaseConfig

class MySqlJdbcSetting(url: String, user: String, password: String) : MySqlSetting<DatabaseConfig> {
    override val config: DatabaseConfig =
        object : DefaultDatabaseConfig(url, user, password) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
