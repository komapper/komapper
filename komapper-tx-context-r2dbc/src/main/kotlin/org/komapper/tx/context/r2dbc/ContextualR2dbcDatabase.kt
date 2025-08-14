package org.komapper.tx.context.r2dbc

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
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

interface ContextualR2dbcDatabase : Database {
    /**
     * Runs the given [query] and returns the result.
     *
     * @param query the query
     * @return the result represented by the query
     */
    context(r2dbcContext: R2dbcContext)
    suspend fun <T> runQuery(query: Query<T>): T

    /**
     * Runs the given [block] and returns the result.
     *
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    context(r2dbcContext: R2dbcContext)
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T

    /**
     * Converts the given [query] to [Flow].
     *
     * @param query the query
     * @return the flow
     */
    context(r2dbcContext: R2dbcContext)
    fun <T> flowQuery(query: FlowQuery<T>): Flow<T>

    /**
     * Converts the given [block] to [Flow].
     *
     * @param block the block that returns a query
     * @return the flow
     */
    context(r2dbcContext: R2dbcContext)
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
        block: suspend context(R2dbcContext)
        () -> R,
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
        block: suspend context(R2dbcContext)
        FlowCollector<R>.() -> Unit,
    ): Flow<R>

    fun unwrap(): R2dbcDatabase
}

internal class ContextualR2dbcDatabaseImpl(
    private val database: R2dbcDatabase,
    private val transactionManager: ContextualR2dbcTransactionManager,
    private val coroutineTransactionOperator: ContextualR2dbcCoroutineTransactionOperator,
    private val flowTransactionOperator: ContextualR2dbcFlowTransactionOperator,
) : ContextualR2dbcDatabase {
    override val config: R2dbcDatabaseConfig
        get() = database.config

    @Suppress("UNCHECKED_CAST")
    context(r2dbcContext: R2dbcContext)
    override suspend fun <T> runQuery(query: Query<T>): T {
        val runtimeConfig = object : R2dbcDatabaseConfig by config {
            override val session: R2dbcSession = object : R2dbcSession {
                override val connectionFactory: ConnectionFactory
                    get() = config.session.connectionFactory

                override val coroutineTransactionOperator: CoroutineTransactionOperator
                    get() = throw UnsupportedOperationException()

                override val flowTransactionOperator: FlowTransactionOperator
                    get() = throw UnsupportedOperationException()

                override suspend fun getConnection(): io.r2dbc.spi.Connection {
                    return transactionManager.getConnection()
                }

                override suspend fun releaseConnection(connection: io.r2dbc.spi.Connection) {
                    connection.close().asFlow().collect()
                }
            }
        }

        val runner = query.accept(R2dbcQueryVisitor) as R2dbcRunner<T>
        runner.check(runtimeConfig)
        return runner.run(runtimeConfig)
    }

    context(r2dbcContext: R2dbcContext)
    override suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    context(r2dbcContext: R2dbcContext)
    override fun <T> flowQuery(query: FlowQuery<T>): Flow<T> {
        @Suppress("UNCHECKED_CAST")
        val builder = query.accept(R2dbcFlowQueryVisitor) as R2dbcFlowBuilder<T>
        builder.check(config)
        return builder.build(config)
    }

    context(r2dbcContext: R2dbcContext)
    override fun <T> flowQuery(block: QueryScope.() -> FlowQuery<T>): Flow<T> {
        val query = block(QueryScope)
        return flowQuery(query)
    }

    override suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext)
        () -> R,
    ): R {
        val context = R2dbcContext(this, coroutineTransactionOperator, flowTransactionOperator)
        return with(context) {
            when (transactionAttribute) {
                TransactionAttribute.REQUIRED -> coroutineTransactionOperator.required(transactionProperty, block)
                TransactionAttribute.REQUIRES_NEW -> coroutineTransactionOperator.requiresNew(
                    transactionProperty,
                    block,
                )
            }
        }
    }

    override fun <R> flowTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext)
        FlowCollector<R>.() -> Unit,
    ): Flow<R> {
        val context = R2dbcContext(this, coroutineTransactionOperator, flowTransactionOperator)
        return with(context) {
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

fun R2dbcDatabase.asContextualDatabase(): ContextualR2dbcDatabase {
    val transactionManager = ContextualR2dbcTransactionManagerImpl(config.connectionFactory, config.loggerFacade)
    val coroutineTransactionOperator = ContextualR2dbcCoroutineTransactionOperatorImpl(transactionManager)
    val flowTransactionOperator = ContextualR2dbcFlowTransactionOperatorImpl(transactionManager)
    return ContextualR2dbcDatabaseImpl(
        this,
        transactionManager,
        coroutineTransactionOperator,
        flowTransactionOperator,
    )
}
