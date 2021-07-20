package integration.jdbc.setting

import integration.setting.H2Setting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class H2JdbcSetting(url: String) : H2Setting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
