package integration.jdbc.h2

import integration.core.H2Setting
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabaseConfig

class JdbcH2Setting(url: String) : H2Setting<JdbcDatabaseConfig> {
    override val config: JdbcDatabaseConfig =
        object : DefaultJdbcDatabaseConfig(url) {
            override val executionOptions = super.executionOptions.copy(batchSize = 2)
        }
}
