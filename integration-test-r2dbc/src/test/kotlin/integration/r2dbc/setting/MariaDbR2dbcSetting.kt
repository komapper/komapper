package integration.r2dbc.setting

import integration.setting.MariaDbSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MariaDBContainerProvider
import org.testcontainers.containers.MariaDBR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

class MariaDbR2dbcSetting :
    MariaDbSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "mariadb"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = MariaDBContainerProvider().newInstance(connectionUrl) as MariaDBContainer<*>
            val r2dbcContainer = MariaDBR2DBCDatabaseContainer(container)
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
