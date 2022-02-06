package integration.r2dbc.postgresql

import integration.core.PostgreSqlSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainerProvider
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcPostgreSqlSetting : PostgreSqlSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "postgresql"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = PostgreSQLContainerProvider().newInstance(connectionUrl) as PostgreSQLContainer<*>
            val r2dbcContainer = PostgreSQLR2DBCDatabaseContainer(container)
            r2dbcContainer.start()
            r2dbcContainer.configure(
                ConnectionFactoryOptions.builder()
                    .option(ConnectionFactoryOptions.DRIVER, "pool")
                    .option(ConnectionFactoryOptions.PROTOCOL, DRIVER)
                    .option(Option.valueOf("initialSize"), 2)
                    .build()
            )
        }
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(OPTIONS), R2dbcDialects.get(DRIVER))
}
