package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.komapper.core.dsl.query.QueryScope
import org.komapper.r2dbc.dsl.runner.R2dbcQueryRunner

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

    suspend fun <T> runQuery(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): T {
        val runner = getQueryRunner(block)
        return runner.run(config)
    }

    fun <T> dryRunQuery(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): String {
        val runner = getQueryRunner(block)
        return runner.dryRun(config)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getQueryRunner(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): R2dbcQueryRunner<T> {
        val query = block(QueryScope)
        return query.accept(R2dbcQueryVisitor()) as R2dbcQueryRunner<T>
    }
}

internal class R2dbcDatabaseImpl(override val config: R2dbcDatabaseConfig) : R2dbcDatabase
