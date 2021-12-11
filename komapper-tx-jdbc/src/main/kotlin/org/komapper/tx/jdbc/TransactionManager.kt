package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

@ThreadSafe
interface TransactionManager {
    val dataSource: DataSource
    val isActive: Boolean
    val isRollbackOnly: Boolean

    fun setRollbackOnly()
    fun begin(isolationLevel: IsolationLevel? = null)
    fun commit()
    fun suspend(): Transaction
    fun resume(tx: Transaction)
    fun rollback()
}

internal class TransactionManagerImpl(
    private val internalDataSource: DataSource,
    private val loggerFacade: LoggerFacade
) : TransactionManager {
    private val threadLocal = ThreadLocal<Transaction>()
    override val dataSource = object : DataSource by internalDataSource {
        override fun getConnection(): Connection {
            val tx = threadLocal.get()
            if (!tx.isActive()) {
                error("A transaction hasn't yet begun.")
            }
            return tx.connection
        }

        override fun getConnection(username: String?, password: String?): Connection {
            throw UnsupportedOperationException()
        }
    }

    override val isActive: Boolean
        get() {
            val tx = threadLocal.get()
            return tx.isActive()
        }

    override val isRollbackOnly: Boolean
        get() {
            val tx = threadLocal.get()
            return if (tx.isActive()) {
                tx.isRollbackOnly
            } else false
        }

    override fun setRollbackOnly() {
        val tx = threadLocal.get()
        if (tx.isActive()) {
            tx.isRollbackOnly = true
        }
    }

    override fun begin(isolationLevel: IsolationLevel?) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = TransactionImpl {
            val connection = try {
                internalDataSource.connection
            } catch (e: SQLException) {
                throw e
            }
            TransactionConnectionImpl(connection, isolationLevel).apply {
                try {
                    initialize()
                } catch (e: SQLException) {
                    dispose()
                    throw e
                }
            }
        }
        threadLocal.set(tx)
        loggerFacade.begin(tx.id)
    }

    override fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        if (tx.isInitialized()) {
            val connection = tx.connection
            try {
                connection.commit()
                loggerFacade.commit(tx.id)
            } catch (e: SQLException) {
                rollbackInternal(tx)
                throw e
            } finally {
                release(tx)
            }
        } else {
            release(tx)
        }
    }

    override fun suspend(): Transaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        return tx
    }

    override fun resume(tx: Transaction) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
        }
        threadLocal.set(tx)
    }

    override fun rollback() {
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
                loggerFacade.rollback(tx.id)
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

    private fun Transaction?.isActive(): Boolean = this != null
}
