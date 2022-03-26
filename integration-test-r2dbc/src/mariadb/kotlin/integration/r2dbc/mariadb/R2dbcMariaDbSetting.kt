package integration.r2dbc.mariadb

import integration.core.MariaDbSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.dialect.mariadb.r2dbc.R2dbcMariaDbDialect
import org.komapper.r2dbc.R2dbcDatabase
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MariaDBContainerProvider
import org.testcontainers.containers.MariaDBR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcMariaDbSetting(private val driver: String, private val url: String) :
    MariaDbSetting<R2dbcDatabase> {

    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = MariaDBContainerProvider().newInstance(connectionUrl) as MariaDBContainer<*>
        val r2dbcContainer = MariaDBR2DBCDatabaseContainer(container)
        r2dbcContainer.start()
        r2dbcContainer.configure(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, driver)
                .option(Option.valueOf("initialSize"), 2)
                .build()
        )
    }

    override val database: R2dbcDatabase
        get() = R2dbcDatabase(
            ConnectionFactories.get(options),
            R2dbcMariaDbDialect(),
            executionOptions = ExecutionOptions(batchSize = 2)
        )
}
