package org.komapper.tx.context.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.reactive.collect
import org.komapper.core.Database
import org.komapper.core.dsl.query.FlowQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.dsl.runner.R2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.visitor.R2dbcFlowQueryVisitor
import org.komapper.r2dbc.dsl.visitor.R2dbcQueryVisitor
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty

interface ContextAwareR2dbcDatabase : Database {

    /**
     * Runs the given [query] and returns the result.
     *
     * @param query the query
     * @return the result represented by the query
     */
    context(R2dbcContext)
    suspend fun <T> runQuery(query: Query<T>): T

    /**
     * Runs the given [block] and returns the result.
     *
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    context(R2dbcContext)
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T

    /**
     * Converts the given [query] to [Flow].
     *
     * @param query the query
     * @return the flow
     */
    context(R2dbcContext)
    fun <T> flowQuery(query: FlowQuery<T>): Flow<T>

    /**
     * Converts the given [block] to [Flow].
     *
     * @param block the block that returns a query
     * @return the flow
     */
    context(R2dbcContext)
    fun <T> flowQuery(block: QueryScope.() -> FlowQuery<T>): Flow<T>

    /**
     * Begins a R2DBC transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) (tx: ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R

    /**
     * Builds a transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionAttribute the transaction attribute
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> flowTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(tx: ContextAwareR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R>

    fun unwrap(): R2dbcDatabase
}

internal class ContextAwareR2dbcDatabaseImpl(
    private val database: R2dbcDatabase,
    private val transactionManager: ContextAwareR2dbcTransactionManager,
    private val coroutineTransactionOperator: ContextAwareR2dbcCoroutineTransactionOperator,
    private val flowTransactionOperator: ContextAwareR2dbcFlowTransactionOperator,
) : ContextAwareR2dbcDatabase {

    override val config: R2dbcDatabaseConfig
        get() = database.config

    context(R2dbcContext)
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> runQuery(query: Query<T>): T {
        val runtimeConfig = object : R2dbcDatabaseConfig by config {
            override val session: R2dbcSession = object : R2dbcSession {
                override val coroutineTransactionOperator: CoroutineTransactionOperator
                    get() = throw UnsupportedOperationException()

                override val flowTransactionOperator: FlowTransactionOperator
                    get() = throw UnsupportedOperationException()

                override suspend fun getConnection(): io.r2dbc.spi.Connection {
                    return transactionManager.getConnection()
                }

                override suspend fun releaseConnection(connection: io.r2dbc.spi.Connection) {
                    connection.close().collect { }
                }
            }
        }

        val runner = query.accept(R2dbcQueryVisitor) as R2dbcRunner<T>
        runner.check(runtimeConfig)
        return runner.run(runtimeConfig)
    }

    context(R2dbcContext)
    override suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    context(R2dbcContext)
    override fun <T> flowQuery(query: FlowQuery<T>): Flow<T> {
        @Suppress("UNCHECKED_CAST")
        val builder = query.accept(R2dbcFlowQueryVisitor) as R2dbcFlowBuilder<T>
        builder.check(config)
        return builder.build(config)
    }

    context(R2dbcContext)
    override fun <T> flowQuery(block: QueryScope.() -> FlowQuery<T>): Flow<T> {
        val query = block(QueryScope)
        return flowQuery(query)
    }

    override suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) (ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R {
        val r2dbcContext = R2dbcContext(this)
        return with(r2dbcContext) {
            when (transactionAttribute) {
                TransactionAttribute.REQUIRED -> coroutineTransactionOperator.required(transactionProperty, block)
                TransactionAttribute.REQUIRES_NEW -> coroutineTransactionOperator.requiresNew(
                    transactionProperty,
                    block
                )
            }
        }
    }

    override fun <R> flowTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextAwareR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        val r2dbcContext = R2dbcContext(this)
        return with(r2dbcContext) {
            when (transactionAttribute) {
                TransactionAttribute.REQUIRED -> flowTransactionOperator.required(transactionProperty, block)
                TransactionAttribute.REQUIRES_NEW -> flowTransactionOperator.requiresNew(transactionProperty, block)
            }
        }
    }

    override fun unwrap(): R2dbcDatabase {
        return database
    }
}

fun R2dbcDatabase.asContextAwareDatabase(): ContextAwareR2dbcDatabase {
    val transactionManager = ContextAwareR2dbcTransactionManagerImpl(config.connectionFactory, config.loggerFacade)
    val coroutineTransactionOperator = ContextAwareR2dbcCoroutineTransactionOperatorImpl(transactionManager)
    val flowTransactionOperator = ContextAwareR2dbcFlowTransactionOperatorImpl(transactionManager)
    return ContextAwareR2dbcDatabaseImpl(
        this,
        transactionManager, coroutineTransactionOperator, flowTransactionOperator
    )
}
