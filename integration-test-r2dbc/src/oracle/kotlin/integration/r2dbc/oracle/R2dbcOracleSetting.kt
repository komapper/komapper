package integration.r2dbc.oracle

import integration.core.OracleSetting
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.OracleContainerProvider
import org.testcontainers.jdbc.ConnectionUrl
import org.testcontainers.lifecycle.Startable
import org.testcontainers.r2dbc.R2DBCDatabaseContainer

@Suppress("unused")
class R2dbcOracleSetting(private val url: String) : OracleSetting<R2dbcDatabase> {
    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = OracleContainerProvider().newInstance(connectionUrl) as OracleContainer
        val r2dbcContainer = OracleR2DBCDatabaseContainer(container)
        r2dbcContainer.start()
        r2dbcContainer.configure(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "oracle")
                .option(Option.valueOf("initialSize"), 2)
                .build(),
        )
    }

    override val database: R2dbcDatabase
        get() = R2dbcDatabase(
            options = options,
            executionOptions = ExecutionOptions(batchSize = 2),
        )
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
