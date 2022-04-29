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
    private val dataSource: DataSource,
    private val loggerFacade: LoggerFacade
) : JdbcTransactionManager {

    private val transactionContext = ThreadLocal<JdbcTransaction>()

    override fun getConnection(): Connection {
        val tx = transactionContext.get()
        return tx?.connection ?: dataSource.connection
    }

    override fun isActive(): Boolean {
        val tx = transactionContext.get()
        return tx != null
    }

    override fun isRollbackOnly(): Boolean {
        val tx = transactionContext.get()
        return tx?.isRollbackOnly ?: false
    }

    override fun setRollbackOnly() {
        val tx = transactionContext.get()
        if (tx != null) {
            tx.isRollbackOnly = true
        }
    }

    override fun begin(transactionProperty: TransactionProperty) {
        val currentTx = transactionContext.get()
        if (currentTx != null) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val isolationLevel = transactionProperty[TransactionProperty.IsolationLevel]
        val readOnly = transactionProperty[TransactionProperty.ReadOnly]
        val txCon = JdbcTransactionConnection(dataSource.connection, isolationLevel, readOnly)
        val name = transactionProperty[TransactionProperty.Name]
        val tx = JdbcTransaction(name?.value, txCon)
        runCatching {
            tx.connection.initialize()
        }.onSuccess {
            loggerFacade.begin(tx.toString())
        }.onFailure {
            release(tx)
        }.getOrThrow()
        transactionContext.set(tx)
    }

    override fun commit() {
        val tx = transactionContext.get() ?: error("A transaction hasn't yet begun.")
        runCatching {
            tx.connection.commit()
        }.also {
            release(tx)
        }.onSuccess {
            loggerFacade.commit(tx.toString())
        }.onFailure { cause ->
            runCatching {
                loggerFacade.commitFailed(tx.toString(), cause)
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.getOrThrow()
    }

    override fun suspend(): JdbcTransaction {
        val tx = transactionContext.get() ?: error("A transaction hasn't yet begun.")
        transactionContext.remove()
        loggerFacade.suspend(tx.toString())
        return tx
    }

    override fun resume(tx: JdbcTransaction) {
        val currentTx = transactionContext.get()
        if (currentTx != null) {
            rollbackInternal(currentTx)
        }
        transactionContext.set(tx)
        loggerFacade.resume(tx.toString())
    }

    override fun rollback() {
        val tx = transactionContext.get() ?: return
        rollbackInternal(tx)
    }

    /**
     * This function must not throw any exceptions.
     */
    private fun rollbackInternal(tx: JdbcTransaction) {
        runCatching {
            tx.connection.rollback()
        }.also {
            release(tx)
        }.onSuccess {
            runCatching {
                loggerFacade.rollback(tx.toString())
            }
        }.onFailure { cause ->
            runCatching {
                loggerFacade.rollbackFailed(tx.toString(), cause)
            }
        }
    }

    /**
     * This function must not throw any exceptions.
     */
    private fun release(tx: JdbcTransaction) {
        transactionContext.remove()
        runCatching {
            tx.connection.reset()
        }
        runCatching {
            tx.connection.dispose()
        }
    }
}
