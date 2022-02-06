package integration.r2dbc.sqlserver

import integration.core.SqlServerSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.MSSQLR2DBCDatabaseContainer
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MSSQLServerContainerProvider
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcSqlServerSetting : SqlServerSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "sqlserver"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = MSSQLServerContainerProvider().newInstance(connectionUrl) as MSSQLServerContainer<*>
            val r2dbcContainer = MSSQLR2DBCDatabaseContainer(container)
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
