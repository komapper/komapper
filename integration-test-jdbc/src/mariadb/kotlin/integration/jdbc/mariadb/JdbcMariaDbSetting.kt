package integration.jdbc.mariadb

import integration.setting.MariaDbSetting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class JdbcMariaDbSetting(url: String) : MariaDbSetting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url, "test", "test") {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
