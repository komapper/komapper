package integration.r2dbc.setting

import integration.setting.PostgreSqlSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

class PostgreSqlR2dbcSetting(driver: String, database: String, user: String, password: String) : PostgreSqlSetting<R2dbcDatabaseConfig> {

    private val options: ConnectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.DRIVER, driver)
        .option(ConnectionFactoryOptions.HOST, "localhost")
        .option(ConnectionFactoryOptions.DATABASE, database)
        .option(ConnectionFactoryOptions.USER, user)
        .option(ConnectionFactoryOptions.PASSWORD, password)
        .build()

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(options), R2dbcDialect.load(driver))
}
