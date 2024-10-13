package integration.r2dbc.postgresql

import integration.core.PostgreSqlSetting
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase
import org.testcontainers.containers.PostgisContainerProvider
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class R2dbcPostgreSqlSetting(private val url: String) : PostgreSqlSetting<R2dbcDatabase> {
    private val options: ConnectionFactoryOptions by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = PostgisContainerProvider().newInstance(connectionUrl) as PostgreSQLContainer<*>
        val r2dbcContainer = PostgreSQLR2DBCDatabaseContainer(container)
        r2dbcContainer.start()
        r2dbcContainer.configure(
            // We do not use connection pool to use the `mood` enum codec
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                .build(),
        )
    }

    override val database: R2dbcDatabase
        get() = R2dbcDatabase(
            options = options,
            executionOptions = ExecutionOptions(batchSize = 2),
        )
}
