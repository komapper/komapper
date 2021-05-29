package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.r2dbc.dsl.query.Query
import org.komapper.r2dbc.dsl.query.R2dbcQueryScope

interface R2dbcDatabase {

    companion object {

        fun create(config: R2dbcDatabaseConfig): R2dbcDatabase {
            return R2dbcDatabaseImpl(config)
        }

        fun create(
            connectionFactory: ConnectionFactory,
            dialect: R2dbcDialect,
        ): R2dbcDatabase {
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, dialect)
            return create(config)
        }

        fun create(options: ConnectionFactoryOptions): R2dbcDatabase {
            val driver = options.getValue(ConnectionFactoryOptions.DRIVER)
            checkNotNull(driver) { "The driver option is not found." }
            val connectionFactory = ConnectionFactories.get(options)
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, R2dbcDialect.load(driver))
            return create(config)
        }

        fun create(url: String): R2dbcDatabase {
            val connectionFactory = ConnectionFactories.get(url)
            val driver = R2dbcDialect.extractR2dbcDriver(url)
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, R2dbcDialect.load(driver))
            return create(config)
        }
    }

    val config: R2dbcDatabaseConfig

    suspend fun <T> runQuery(block: R2dbcQueryScope.() -> Query<T>): T {
        return block(R2dbcQueryScope).run(this.config)
    }
}

internal class R2dbcDatabaseImpl(override val config: R2dbcDatabaseConfig) : R2dbcDatabase
