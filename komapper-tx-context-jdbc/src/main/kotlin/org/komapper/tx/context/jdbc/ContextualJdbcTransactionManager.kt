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

    context(JdbcTransactionContext)
    fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    context(JdbcTransactionContext)
    fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(JdbcTransactionContext)
    fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(JdbcTransactionContext)
    fun setRollbackOnly()

    context(JdbcTransactionContext)
    fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty): JdbcTransactionContext

    context(JdbcTransactionContext)
    fun commit()

    context(JdbcTransactionContext)
    fun suspend(): JdbcTransactionContext

    /**
     * This function must not throw any exceptions.
     */
    context(JdbcTransactionContext)
    fun resume()

    /**
     * This function must not throw any exceptions.
     */
    context(JdbcTransactionContext)
    fun rollback()
}

internal class ContextualJdbcTransactionManagerImpl(
    dataSource: DataSource,
    loggerFacade: LoggerFacade
) : ContextualJdbcTransactionManager {

    private val management: JdbcTransactionManagement = JdbcTransactionManagement(dataSource, loggerFacade)

    context(JdbcTransactionContext)
    override fun getConnection(): Connection {
        val tx = transaction
        return management.getConnection(tx)
    }

    context(JdbcTransactionContext)
    override fun isActive(): Boolean {
        val tx = transaction
        return management.isActive(tx)
    }

    context(JdbcTransactionContext)
    override fun isRollbackOnly(): Boolean {
        val tx = transaction
        return management.isRollbackOnly(tx)
    }

    context(JdbcTransactionContext)
    override fun setRollbackOnly() {
        val tx = transaction
        management.setRollbackOnly(tx)
    }

    context(JdbcTransactionContext)
    override fun begin(transactionProperty: TransactionProperty): JdbcTransactionContext {
        val currentTx = transaction
        val tx = management.begin(currentTx, transactionProperty)
        return JdbcTransactionContext(tx)
    }

    context(JdbcTransactionContext)
    override fun commit() {
        val tx = transaction
        management.commit(tx)
    }

    context(JdbcTransactionContext)
    override fun suspend(): JdbcTransactionContext {
        val tx = transaction
        management.suspend(tx)
        return EmptyJdbcTransactionContext
    }

    context(JdbcTransactionContext)
    override fun resume() {
        val tx = transaction
        management.resume(tx)
    }

    context(JdbcTransactionContext)
    override fun rollback() {
        val tx = transaction
        management.rollback(tx)
    }
}
