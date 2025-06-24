package org.komapper.tx.context.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty
import org.komapper.tx.jdbc.JdbcTransactionManagement
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface ContextualJdbcTransactionManager {
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun setRollbackOnly()

    context(jdbcTransactionContext: JdbcTransactionContext)
    fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty): JdbcTransactionContext

    context(jdbcTransactionContext: JdbcTransactionContext)
    fun commit()

    context(jdbcTransactionContext: JdbcTransactionContext)
    fun suspend(): JdbcTransactionContext

    /**
     * This function must not throw any exceptions.
     */
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun resume()

    /**
     * This function must not throw any exceptions.
     */
    context(jdbcTransactionContext: JdbcTransactionContext)
    fun rollback()
}

internal class ContextualJdbcTransactionManagerImpl(
    dataSource: DataSource,
    loggerFacade: LoggerFacade,
) : ContextualJdbcTransactionManager {
    private val management: JdbcTransactionManagement = JdbcTransactionManagement(dataSource, loggerFacade)

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun getConnection(): Connection {
        val tx = jdbcTransactionContext.transaction
        return management.getConnection(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun isActive(): Boolean {
        val tx = jdbcTransactionContext.transaction
        return management.isActive(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun isRollbackOnly(): Boolean {
        val tx = jdbcTransactionContext.transaction
        return management.isRollbackOnly(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun setRollbackOnly() {
        val tx = jdbcTransactionContext.transaction
        management.setRollbackOnly(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun begin(transactionProperty: TransactionProperty): JdbcTransactionContext {
        val currentTx = jdbcTransactionContext.transaction
        val tx = management.begin(currentTx, transactionProperty)
        return JdbcTransactionContext(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun commit() {
        val tx = jdbcTransactionContext.transaction
        management.commit(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun suspend(): JdbcTransactionContext {
        val tx = jdbcTransactionContext.transaction
        management.suspend(tx)
        return EmptyJdbcTransactionContext
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun resume() {
        val tx = jdbcTransactionContext.transaction
        management.resume(tx)
    }

    context(jdbcTransactionContext: JdbcTransactionContext)
    override fun rollback() {
        val tx = jdbcTransactionContext.transaction
        management.rollback(tx)
    }
}
