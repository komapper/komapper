package integration.r2dbc.setting

import integration.setting.PostgreSqlSetting
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainerProvider
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer
import org.testcontainers.jdbc.ConnectionUrl

class PostgreSqlR2dbcSetting : PostgreSqlSetting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "postgresql"
        val CONTAINER: PostgreSQLR2DBCDatabaseContainer by lazy {
            val url = System.getProperty("url") ?: error("The url property is not found.")
            val connectionUrl = ConnectionUrl.newInstance(url)
            val containerProvider = PostgreSQLContainerProvider()
            val container = containerProvider.newInstance(connectionUrl) as PostgreSQLContainer<*>
            PostgreSQLR2DBCDatabaseContainer(container).apply {
                start()
            }
        }
        val OPTIONS: ConnectionFactoryOptions = CONTAINER.configure(
            ConnectionFactoryOptions.builder().option(ConnectionFactoryOptions.DRIVER, DRIVER).build()
        )
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(OPTIONS), R2dbcDialect.load(DRIVER))

    override fun close() {
        CONTAINER.close()
    }
}
