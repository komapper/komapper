package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.komapper.core.TransactionAttribute
import org.komapper.core.dsl.query.FlowQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.r2dbc.dsl.runner.R2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.visitor.R2dbcFlowQueryVisitor
import org.komapper.r2dbc.dsl.visitor.R2dbcQueryVisitor

/**
 * Represents a database accessed by R2DBC.
 */
interface R2dbcDatabase {

    companion object {

        /**
         * Creates a [R2dbcDatabase] instance.
         *
         * @param config the database configuration
         */
        fun create(config: R2dbcDatabaseConfig): R2dbcDatabase {
            return R2dbcDatabaseImpl(config)
        }

        /**
         * Creates a [R2dbcDatabase] instance.
         *
         * @param connectionFactory the connection factory
         * @param dialect the dialect
         */
        fun create(
            connectionFactory: ConnectionFactory,
            dialect: R2dbcDialect,
        ): R2dbcDatabase {
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, dialect)
            return create(config)
        }

        /**
         * Creates a [R2dbcDatabase] instance.
         *
         * @param options the connection factory options
         */
        fun create(options: ConnectionFactoryOptions): R2dbcDatabase {
            val driver = options.getValue(ConnectionFactoryOptions.DRIVER)?.toString()
            checkNotNull(driver) { "The driver option is not found." }
            val connectionFactory = ConnectionFactories.get(options)
            val dialect = R2dbcDialects.get(driver)
            val config = DefaultR2dbcDatabaseConfig(connectionFactory, dialect)
            return create(config)
        }

        /**
         * Creates a [R2dbcDatabase] instance.
         *
         * @param url the R2DBC URL
         */
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
     *
     * @param query the query
     * @return the result represented by the query
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> runQuery(query: Query<T>): T {
        val runner = query.accept(R2dbcQueryVisitor) as R2dbcRunner<T>
        runner.check(config)
        return runner.run(config)
    }

    /**
     * Runs the given [block] and returns the result.
     *
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    /**
     * Converts the given [query] to [Flow].
     *
     * @param query the query
     * @return the flow
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> flowQuery(query: FlowQuery<T>): Flow<T> {
        val builder = query.accept(R2dbcFlowQueryVisitor) as R2dbcFlowBuilder<T>
        builder.check(config)
        return builder.build(config)
    }

    /**
     * Converts the given [block] to [Flow].
     *
     * @param block the block that returns a query
     * @return the flow
     */
    fun <T> flowQuery(block: QueryScope.() -> FlowQuery<T>): Flow<T> {
        val query = block(QueryScope)
        return flowQuery(query)
    }

    /**
     * Begins a R2DBC transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionDefinition the transactionDefinition level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend (R2dbcCoroutineTransactionOperator) -> R
    ): R {
        val tx = config.session.coroutineTransactionOperator
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> tx.required(transactionDefinition, block)
            TransactionAttribute.REQUIRES_NEW -> tx.requiresNew(transactionDefinition, block)
        }
    }

    /**
     * Builds a transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionAttribute the transaction attribute
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> flowTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        val tx = config.session.flowTransactionOperator
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> tx.required(transactionDefinition, block)
            TransactionAttribute.REQUIRES_NEW -> tx.requiresNew(transactionDefinition, block)
        }
    }
}

internal class R2dbcDatabaseImpl(override val config: R2dbcDatabaseConfig) : R2dbcDatabase
