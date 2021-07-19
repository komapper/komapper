package integration.r2dbc.setting

import integration.setting.MySqlSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.MySQLContainerProvider
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

class MySqlR2dbcSetting :
    MySqlSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "mysql"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = MySQLContainerProvider().newInstance(connectionUrl) as MySQLContainer<*>
            val r2dbcContainer = MySQLR2DBCDatabaseContainer(container)
            r2dbcContainer.start()
            r2dbcContainer.configure(
                ConnectionFactoryOptions.builder().option(
                    ConnectionFactoryOptions.DRIVER, DRIVER
                ).build()
            )
        }
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(OPTIONS), R2dbcDialect.load(DRIVER))
}
