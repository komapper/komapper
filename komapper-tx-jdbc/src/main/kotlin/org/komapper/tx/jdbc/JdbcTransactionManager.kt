package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty
import java.sql.Connection
import javax.sql.DataSource

/**
 * The JDBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface JdbcTransactionManager {

    fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    fun setRollbackOnly()

    fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty)

    fun commit()

    fun suspend(): JdbcTransaction

    /**
     * This function must not throw any exceptions.
     */
    fun resume(tx: JdbcTransaction)

    /**
     * This function must not throw any exceptions.
     */
    fun rollback()
}

internal class JdbcTransactionManagerImpl(
    dataSource: DataSource,
    loggerFacade: LoggerFacade,
) : JdbcTransactionManager {

    private val transactionContext = ThreadLocal<JdbcTransaction>()

    private val management: JdbcTransactionManagement =
        JdbcTransactionManagement(dataSource, loggerFacade) { transactionContext.remove() }

    override fun getConnection(): Connection {
        val tx = transactionContext.get()
        return management.getConnection(tx)
    }

    override fun isActive(): Boolean {
        val tx = transactionContext.get()
        return management.isActive(tx)
    }

    override fun isRollbackOnly(): Boolean {
        val tx = transactionContext.get()
        return management.isRollbackOnly(tx)
    }

    override fun setRollbackOnly() {
        val tx = transactionContext.get()
        management.setRollbackOnly(tx)
    }

    override fun begin(transactionProperty: TransactionProperty) {
        val currentTx = transactionContext.get()
        val tx = management.begin(currentTx, transactionProperty)
        transactionContext.set(tx)
    }

    override fun commit() {
        val tx = transactionContext.get()
        management.commit(tx)
    }

    override fun suspend(): JdbcTransaction {
        val tx = transactionContext.get()
        management.suspend(tx)
        transactionContext.remove()
        return tx
    }

    override fun resume(tx: JdbcTransaction) {
        management.resume(tx)
        transactionContext.set(tx)
    }

    override fun rollback() {
        val tx = transactionContext.get()
        management.rollback(tx)
    }
}
