package integration.r2dbc.setting

import integration.setting.H2Setting
import io.r2dbc.spi.ConnectionFactories
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

class H2R2dbcSetting : H2Setting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "h2"
        val URL: String = System.getProperty("url") ?: error("The url property is not found.")
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(URL), R2dbcDialect.load(DRIVER))
}
