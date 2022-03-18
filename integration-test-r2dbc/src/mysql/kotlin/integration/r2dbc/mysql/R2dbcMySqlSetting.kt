package integration.r2dbc.mysql

import integration.core.MySqlSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDialects
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.MySQLContainerProvider
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcMySqlSetting(private val driver: String, private val url: String) :
    MySqlSetting<R2dbcDatabase> {

    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = MySQLContainerProvider().newInstance(connectionUrl) as MySQLContainer<*>
        val r2dbcContainer = MySQLR2DBCDatabaseContainer(container)
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
            R2dbcDialects.get(driver),
            executionOptions = ExecutionOptions(batchSize = 2)
        )
}
