package integration.r2dbc.setting

import integration.setting.OracleSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.OracleContainerProvider
import org.testcontainers.jdbc.ConnectionUrl
import org.testcontainers.lifecycle.Startable
import org.testcontainers.r2dbc.R2DBCDatabaseContainer

class OracleR2dbcSetting : OracleSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "oracle"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = OracleContainerProvider().newInstance(connectionUrl) as OracleContainer
            val r2dbcContainer = OracleR2DBCDatabaseContainer(container)
            r2dbcContainer.start()
            r2dbcContainer.configure(
                ConnectionFactoryOptions.builder().option(
                    ConnectionFactoryOptions.DRIVER, DRIVER
                ).build()
            )
        }
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(OPTIONS), R2dbcDialects.get(DRIVER))
}

class OracleR2DBCDatabaseContainer(private val container: OracleContainer) : R2DBCDatabaseContainer {
    override fun configure(options: ConnectionFactoryOptions): ConnectionFactoryOptions {
        return options.mutate().option(ConnectionFactoryOptions.HOST, container.host)
            .option(ConnectionFactoryOptions.PORT, container.oraclePort)
            .option(ConnectionFactoryOptions.DATABASE, container.databaseName)
            .option(ConnectionFactoryOptions.USER, container.username)
            .option(ConnectionFactoryOptions.PASSWORD, container.password).build()
    }

    override fun getDependencies(): Set<Startable> {
        return container.dependencies
    }

    override fun start() {
        container.start()
    }

    override fun stop() {
        container.stop()
    }

    override fun close() {
        container.close()
    }
}
