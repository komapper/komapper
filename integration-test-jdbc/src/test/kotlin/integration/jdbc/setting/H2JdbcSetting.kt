package integration.jdbc.setting

import integration.setting.H2Setting
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.DefaultDatabaseConfig

class H2JdbcSetting(url: String, user: String, password: String) : H2Setting<DatabaseConfig> {
    override val config: DatabaseConfig =
        object : DefaultDatabaseConfig(url, user, password) {
            override val executionOption = super.executionOption.copy(batchSize = 2)
        }
}
