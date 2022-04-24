package org.komapper.tx.context.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty
import org.komapper.tx.jdbc.JdbcTransaction
import org.komapper.tx.jdbc.JdbcTransactionConnection
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface ContextAwareJdbcTransactionManager {

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

internal class ContextAwareJdbcTransactionManagerImpl(
    private val dataSource: DataSource,
    private val loggerFacade: LoggerFacade
) : ContextAwareJdbcTransactionManager {

    context(JdbcTransactionContext)
        override fun getConnection(): Connection {
        return transaction?.connection ?: dataSource.connection
    }

    context(JdbcTransactionContext)
        override fun isActive(): Boolean {
        return transaction != null
    }

    context(JdbcTransactionContext)
        override fun isRollbackOnly(): Boolean {
        return transaction?.isRollbackOnly ?: false
    }

    context(JdbcTransactionContext)
        override fun setRollbackOnly() {
        val tx = transaction
        if (tx != null) {
            tx.isRollbackOnly = true
        }
    }

    context(JdbcTransactionContext)
        override fun begin(transactionProperty: TransactionProperty): JdbcTransactionContext {
        val currentTx = transaction
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
        return JdbcTransactionContext(tx)
    }

    context(JdbcTransactionContext)
        override fun commit() {
        val tx = transaction ?: error("A transaction hasn't yet begun.")
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

    context(JdbcTransactionContext)
        override fun suspend(): JdbcTransactionContext {
        val tx = transaction ?: error("A transaction hasn't yet begun.")
        loggerFacade.suspend(tx.toString())
        return EmptyJdbcTransactionContext
    }

    context(JdbcTransactionContext)
        override fun resume() {
        if (transaction == null) {
            error("A transaction is not found.")
        }
        loggerFacade.resume(transaction.toString())
    }

    context(JdbcTransactionContext)
        override fun rollback() {
        val tx = transaction ?: return
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
        runCatching {
            tx.connection.reset()
        }
        runCatching {
            tx.connection.dispose()
        }
    }
}

