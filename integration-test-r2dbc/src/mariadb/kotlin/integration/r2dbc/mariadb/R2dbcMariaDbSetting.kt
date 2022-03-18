package integration.r2dbc.mariadb

import integration.core.MariaDbSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MariaDBContainerProvider
import org.testcontainers.containers.MariaDBR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcMariaDbSetting :
    MariaDbSetting<R2dbcDatabase> {
    companion object {
        const val DRIVER: String = "mariadb"
        val OPTIONS: ConnectionFactoryOptions by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val container = MariaDBContainerProvider().newInstance(connectionUrl) as MariaDBContainer<*>
            val r2dbcContainer = MariaDBR2DBCDatabaseContainer(container)
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

    override val database: R2dbcDatabase
        get() = R2dbcDatabase(
            ConnectionFactories.get(OPTIONS),
            R2dbcDialects.get(DRIVER),
            executionOptions = ExecutionOptions(batchSize = 2)
        )
}
