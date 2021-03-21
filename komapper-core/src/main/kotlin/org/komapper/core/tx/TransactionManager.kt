package org.komapper.core.tx

import org.komapper.core.logging.Logger
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class TransactionManager(
    internal val dataSource: DataSource,
    private val logger: Logger
) {
    private val threadLocal = ThreadLocal<Transaction>()
    private val transactionDataSource = object : DataSource by dataSource {
        override fun getConnection(): Connection {
            val tx = threadLocal.get()
            if (!tx.isActive()) {
                throw TransactionException("A transaction hasn't yet begun.")
            }
            return tx.connection
        }

        override fun getConnection(username: String?, password: String?): Connection {
            throw UnsupportedOperationException()
        }
    }

    val isActive: Boolean
        get() {
            val tx = threadLocal.get()
            return tx.isActive()
        }

    val isRollbackOnly: Boolean
        get() {
            val tx = threadLocal.get()
            return if (tx.isActive()) {
                tx.isRollbackOnly
            } else false
        }

    fun setRollbackOnly() {
        val tx = threadLocal.get()
        if (tx.isActive()) {
            tx.isRollbackOnly = true
        }
    }

    fun begin(isolationLevel: TransactionIsolationLevel?) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            throw TransactionException("The transaction \"$currentTx\" already has begun.")
        }
        val tx = Transaction {
            val connection = try {
                dataSource.connection
            } catch (e: SQLException) {
                throw TransactionException(e)
            }
            TransactionConnection(connection, isolationLevel).apply {
                try {
                    initialize()
                } catch (e: SQLException) {
                    dispose()
                    throw TransactionException(e)
                }
            }
        }
        threadLocal.set(tx)
        logger.logTxMessage { "The transaction \"$tx\" has begun." }
    }

    fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            throw TransactionException("A transaction hasn't yet begun.")
        }
        if (tx.isInitialized()) {
            val connection = tx.connection
            try {
                connection.commit()
                logger.logTxMessage { "The transaction \"$tx\" has committed." }
            } catch (e: SQLException) {
                rollbackInternal(tx)
                throw TransactionException(e)
            } finally {
                release(tx)
            }
        } else {
            release(tx)
        }
    }

    fun suspend(): Transaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            throw TransactionException("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        return tx
    }

    fun resume(tx: Transaction) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
        }
        threadLocal.set(tx)
    }

    fun rollback() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            return
        }
        rollbackInternal(tx)
    }

    private fun rollbackInternal(tx: Transaction) {
        if (tx.isInitialized()) {
            val connection = tx.connection
            try {
                connection.rollback()
                logger.logTxMessage { "The transaction \"$tx\" has rolled back." }
            } catch (ignored: SQLException) {
            } finally {
                release(tx)
            }
        } else {
            release(tx)
        }
    }

    private fun release(tx: Transaction) {
        threadLocal.remove()
        if (tx.isInitialized()) {
            val connection = tx.connection
            try {
                connection.reset()
            } catch (ignored: SQLException) {
            }
            connection.dispose()
        }
    }

    fun getDataSource(): DataSource = transactionDataSource

    private fun Transaction?.isActive(): Boolean = this != null
}
