package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.query.FlowQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.r2dbc.dsl.runner.FlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.visitor.R2dbcFlowQueryVisitor
import org.komapper.r2dbc.dsl.visitor.R2dbcQueryVisitor

/**
 * Represents a database accessed by R2DBC.
 */
interface R2dbcDatabase : R2dbc {

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
            val dialect = R2dbcDialects.get(driver)
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, dialect)
            return create(config)
        }

        fun create(url: String): R2dbcDatabase {
            val connectionFactory = ConnectionFactories.get(url)
            val driver = R2dbcDialects.extractR2dbcDriver(url)
            val dialect = R2dbcDialects.get(driver)
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, dialect)
            return create(config)
        }
    }

    /**
     * The database configuration.
     */
    val config: R2dbcDatabaseConfig

    /**
     * Runs the given [query] and returns the result.
     * @param query the query
     * @return the result represented by the query
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> runQuery(query: Query<T>): T {
        val runner = query.accept(R2dbcQueryVisitor) as R2dbcRunner<T>
        return runner.run(config)
    }

    /**
     * Runs the given [block] and returns the result.
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    /**
     * Converts the given [query] to [Flow].
     * @param query the query
     * @return the flow
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> flow(query: FlowQuery<T>): Flow<T> {
        val builder = query.accept(R2dbcFlowQueryVisitor) as FlowBuilder<T>
        return builder.build(config)
    }

    /**
     * Converts the given [block] to [Flow].
     * @param block the block that returns a query
     * @return the flow
     */
    fun <T> flow(block: QueryScope.() -> FlowQuery<T>): Flow<T> {
        val query = block(QueryScope)
        return flow(query)
    }
}

internal class R2dbcDatabaseImpl(override val config: R2dbcDatabaseConfig) : R2dbcDatabase
