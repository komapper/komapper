package integration.r2dbc.oracle

import integration.core.OracleSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.OracleContainerProvider
import org.testcontainers.jdbc.ConnectionUrl
import org.testcontainers.lifecycle.Startable
import org.testcontainers.r2dbc.R2DBCDatabaseContainer

@Suppress("unused")
class R2dbcOracleSetting : OracleSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "oracle"
        private val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = OracleContainerProvider().newInstance(connectionUrl) as OracleContainer
            val r2dbcContainer = OracleR2DBCDatabaseContainer(container)
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
        object : DefaultR2dbcDatabaseConfig(ConnectionFactories.get(OPTIONS), R2dbcDialects.get(DRIVER)) {
            override val executionOptions: ExecutionOptions = super.executionOptions.copy(batchSize = 2)
        }
}

class OracleR2DBCDatabaseContainer(private val container: OracleContainer) : R2DBCDatabaseContainer {
    override fun configure(options: ConnectionFactoryOptions): ConnectionFactoryOptions {
        return options.mutate()
            .option(ConnectionFactoryOptions.HOST, container.host)
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
