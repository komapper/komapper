package org.komapper.tx.context.jdbc

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import java.sql.Connection

interface ContextAwareJdbcDatabase {

    val config: JdbcDatabaseConfig

    val dataFactory: JdbcDataFactory

    /**
     * Runs the given [query] and returns the result.
     * @param query the query
     * @return the result represented by the query
     */
    context(JdbcTransactionContext)
    fun <T> runQuery(query: Query<T>): T

    /**
     * Runs the given [block] and returns the result.
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    context(JdbcTransactionContext)
    fun <T> runQuery(block: QueryScope.() -> Query<T>): T

    /**
     * Begins a JDBC transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> withTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcTransactionContext) (tx: ContextAwareJdbcTransactionOperator) -> R
    ): R
}

internal class ContextAwareJdbcDatabaseImpl(
    override val config: JdbcDatabaseConfig,
    private val transactionManager: ContextAwareJdbcTransactionManager,
    private val transactionOperator: ContextAwareJdbcTransactionOperator
) : ContextAwareJdbcDatabase {
    override val dataFactory: JdbcDataFactory
        get() = config.dataFactory

    context(JdbcTransactionContext)
        override fun <T> runQuery(query: Query<T>): T {
        val runtimeConfig = object : JdbcDatabaseConfig by config {
            override val session: JdbcSession = object : JdbcSession {
                override val transactionOperator: TransactionOperator
                    get() = throw UnsupportedOperationException()

                override fun getConnection(): Connection {
                    return transactionManager.getConnection()
                }

                override fun releaseConnection(connection: Connection) {
                    connection.close()
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val runner = query.accept(JdbcQueryVisitor) as JdbcRunner<T>
        runner.check(runtimeConfig)
        return runner.run(runtimeConfig)
    }

    context(JdbcTransactionContext)
        override fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    override fun <R> withTransaction(
        transactionAttribute: TransactionAttribute,
        transactionProperty: TransactionProperty,
        block: context(JdbcTransactionContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        with(EmptyJdbcTransactionContext) {
            return when (transactionAttribute) {
                TransactionAttribute.REQUIRED -> transactionOperator.required(transactionProperty, block)
                TransactionAttribute.REQUIRES_NEW -> transactionOperator.requiresNew(transactionProperty, block)
            }
        }
    }
}

fun JdbcDatabase.asContextAwareDatabase(): ContextAwareJdbcDatabase {
    val manager = ContextAwareJdbcTransactionManagerImpl(config.dataSource, config.loggerFacade)
    val operator = ContextAwareJdbcTransactionOperatorImpl(manager)
    return ContextAwareJdbcDatabaseImpl(config, manager, operator)
}