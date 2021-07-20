package integration.jdbc.setting

import integration.setting.MariaDbSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class MariaDbJdbcSetting(url: String, user: String, password: String) : MariaDbSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, user, password) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
