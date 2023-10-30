package integration.r2dbc.mysql5

import integration.core.MySql5Setting
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.dialect.mysql.MySqlVersion
import org.komapper.dialect.mysql.r2dbc.MySqlR2dbcDialect
import org.komapper.r2dbc.R2dbcDatabase
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.MySQLContainerProvider
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcMySql5Setting(private val driver: String, private val url: String) :
    MySql5Setting<R2dbcDatabase> {

    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = MySQLContainerProvider().newInstance(connectionUrl) as MySQLContainer<*>
        val r2dbcContainer = MySQLR2DBCDatabaseContainer(container)
        r2dbcContainer.start()
        r2dbcContainer.configure(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "mysql")
                .option(Option.valueOf("initialSize"), 2)
                .build(),
        )
    }

    override val database: R2dbcDatabase
        get() = R2dbcDatabase(
            options = options,
            dialect = MySqlR2dbcDialect(version = MySqlVersion.V5),
            executionOptions = ExecutionOptions(batchSize = 2),
        )
}
