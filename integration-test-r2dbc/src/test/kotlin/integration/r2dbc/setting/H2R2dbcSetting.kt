package integration.r2dbc.setting

import integration.setting.H2Setting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

class H2R2dbcSetting : H2Setting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "h2"
    }

    private val options: ConnectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.DRIVER, DRIVER)
        .option(ConnectionFactoryOptions.PROTOCOL, "mem")
        .option(ConnectionFactoryOptions.DATABASE, "test")
        .option(Option.valueOf("DB_CLOSE_DELAY"), "-1")
        .build()

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(options), R2dbcDialect.load(DRIVER))
}
