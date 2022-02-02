package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import java.sql.Connection
import javax.sql.DataSource

/**
 * The JDBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface JdbcTransactionManager {
    val dataSource: DataSource
    val isActive: Boolean
    val isRollbackOnly: Boolean

    fun setRollbackOnly()
    fun begin(isolationLevel: JdbcIsolationLevel? = null)
    fun commit()
    fun suspend(): JdbcTransaction
    fun resume(tx: JdbcTransaction)
    fun rollback()
}

internal class JdbcTransactionManagerImpl(
    private val internalDataSource: DataSource,
    private val loggerFacade: LoggerFacade
) : JdbcTransactionManager {
    private val threadLocal = ThreadLocal<JdbcTransaction>()
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

    override fun begin(isolationLevel: JdbcIsolationLevel?) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = JdbcTransactionImpl {
            val connection = internalDataSource.connection
            JdbcTransactionConnectionImpl(connection, isolationLevel).apply {
                runCatching {
                    initialize()
                }.onFailure {
                    dispose()
                }.getOrThrow()
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
        val result = if (tx.isInitialized()) {
            val connection = tx.connection
            runCatching {
                connection.commit()
            }.onFailure {
                runCatching {
                    loggerFacade.commitFailed(tx.id, it)
                }
            }.onSuccess {
                loggerFacade.commit(tx.id)
            }
        } else {
            Result.success(Unit)
        }
        release(tx)
        result.getOrThrow()
    }

    override fun suspend(): JdbcTransaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        return tx
    }

    override fun resume(tx: JdbcTransaction) {
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

    private fun rollbackInternal(tx: JdbcTransaction) {
        if (tx.isInitialized()) {
            val connection = tx.connection
            runCatching {
                connection.rollback()
            }.onFailure {
                runCatching {
                    loggerFacade.rollbackFailed(tx.id, it)
                }
            }.onSuccess {
                loggerFacade.rollback(tx.id)
            }
        }
        release(tx)
    }

    private fun release(tx: JdbcTransaction) {
        threadLocal.remove()
        if (tx.isInitialized()) {
            val connection = tx.connection
            connection.reset()
            connection.dispose()
        }
    }
}

private fun JdbcTransaction?.isActive(): Boolean {
    return this != null
}
