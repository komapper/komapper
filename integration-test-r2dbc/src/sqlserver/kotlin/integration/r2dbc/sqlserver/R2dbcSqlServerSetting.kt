package integration.r2dbc.sqlserver

import integration.core.SqlServerSetting
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase
import org.testcontainers.containers.MSSQLR2DBCDatabaseContainer
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MSSQLServerContainerProvider
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcSqlServerSetting(private val url: String) : SqlServerSetting<R2dbcDatabase> {
    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = MSSQLServerContainerProvider().newInstance(connectionUrl) as MSSQLServerContainer<*>
        val r2dbcContainer = MSSQLR2DBCDatabaseContainer(container)
        r2dbcContainer.start()
        r2dbcContainer.configure(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "sqlserver")
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
