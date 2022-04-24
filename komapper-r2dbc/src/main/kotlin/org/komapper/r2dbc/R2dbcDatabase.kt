package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.dsl.query.FlowQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.r2dbc.dsl.runner.R2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.visitor.R2dbcFlowQueryVisitor
import org.komapper.r2dbc.dsl.visitor.R2dbcQueryVisitor
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty

/**
 * Represents a database accessed by R2DBC.
 */
interface R2dbcDatabase : Database {

    /**
     * The database configuration.
     */
    override val config: R2dbcDatabaseConfig

    /**
     * Runs the given [query] and returns the result.
     *
     * @param query the query
     * @return the result represented by the query
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> runQuery(query: Query<T>): T

    /**
     * Runs the given [block] and returns the result.
     *
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T

    /**
     * Converts the given [query] to [Flow].
     *
     * @param query the query
     * @return the flow
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> flowQuery(query: FlowQuery<T>): Flow<T>

    /**
     * Converts the given [block] to [Flow].
     *
     * @param block the block that returns a query
     * @return the flow
     */
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
        block: suspend (CoroutineTransactionOperator) -> R
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
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R>
}

internal class R2dbcDatabaseImpl(override val config: R2dbcDatabaseConfig) : R2dbcDatabase {
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> runQuery(query: Query<T>): T {
        val runner = query.accept(R2dbcQueryVisitor) as R2dbcRunner<T>
        runner.check(config)
        return runner.run(config)
    }

    override suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> flowQuery(query: FlowQuery<T>): Flow<T> {
        val builder = query.accept(R2dbcFlowQueryVisitor) as R2dbcFlowBuilder<T>
        builder.check(config)
        return builder.build(config)
    }

    override fun <T> flowQuery(block: QueryScope.() -> FlowQuery<T>): Flow<T> {
        val query = block(QueryScope)
        return flowQuery(query)
    }

    override suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R {
        val tx = config.session.coroutineTransactionOperator
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> tx.required(transactionProperty, block)
            TransactionAttribute.REQUIRES_NEW -> tx.requiresNew(transactionProperty, block)
        }
    }

    override fun <R> flowTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        val tx = config.session.flowTransactionOperator
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> tx.required(transactionProperty, block)
            TransactionAttribute.REQUIRES_NEW -> tx.requiresNew(transactionProperty, block)
        }
    }
}

/**
 * Creates a [R2dbcDatabase] instance.
 *
 * @param config the database configuration
 */
fun R2dbcDatabase(config: R2dbcDatabaseConfig): R2dbcDatabase {
    return R2dbcDatabaseImpl(config)
}

/**
 * Creates a [R2dbcDatabase] instance.
 *
 * @param connectionFactory the connection factory
 * @param dialect the dialect
 * @param dataTypeProvider the data type provider
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 */
fun R2dbcDatabase(
    connectionFactory: ConnectionFactory,
    dialect: R2dbcDialect,
    dataTypeProvider: R2dbcDataTypeProvider? = null,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions()
): R2dbcDatabase {
    val config = DefaultR2dbcDatabaseConfig(
        connectionFactory = connectionFactory,
        dialect = dialect,
        dataTypeProvider = dataTypeProvider,
        clockProvider = clockProvider,
        executionOptions = executionOptions
    )
    return R2dbcDatabase(config)
}

/**
 * Creates a [R2dbcDatabase] instance.
 *
 * @param options the connection factory options
 * @param dialect the dialect
 * @param dataTypeProvider the data type provider
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 */
fun R2dbcDatabase(
    options: ConnectionFactoryOptions,
    dialect: R2dbcDialect = R2dbcDialects.getByOptions(options),
    dataTypeProvider: R2dbcDataTypeProvider? = null,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions()
): R2dbcDatabase {
    val connectionFactory = ConnectionFactories.get(options)
    val config = DefaultR2dbcDatabaseConfig(
        connectionFactory = connectionFactory,
        dialect = dialect,
        dataTypeProvider = dataTypeProvider,
        clockProvider = clockProvider,
        executionOptions = executionOptions
    )
    return R2dbcDatabase(config)
}

/**
 * Creates a [R2dbcDatabase] instance.
 *
 * @param url the R2DBC URL
 * @param dialect the dialect
 * @param dataTypeProvider the data type provider
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 */
fun R2dbcDatabase(
    url: String,
    dialect: R2dbcDialect = R2dbcDialects.getByUrl(url),
    dataTypeProvider: R2dbcDataTypeProvider? = null,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions()
): R2dbcDatabase {
    val connectionFactory = ConnectionFactories.get(url)
    val config = DefaultR2dbcDatabaseConfig(
        connectionFactory = connectionFactory,
        dialect = dialect,
        dataTypeProvider = dataTypeProvider,
        clockProvider = clockProvider,
        executionOptions = executionOptions
    )
    return R2dbcDatabase(config)
}
