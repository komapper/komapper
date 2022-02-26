package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcIsolationLevel
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

    /**
     * This function must not throw any exceptions.
     */
    fun setRollbackOnly()

    fun begin(isolationLevel: JdbcIsolationLevel? = null)

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
                }.onFailure { cause ->
                    runCatching {
                        dispose()
                    }.onFailure {
                        cause.addSuppressed(it)
                    }
                }.getOrThrow()
            }
        }
        loggerFacade.begin(tx.id)
        threadLocal.set(tx)
    }

    override fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        runCatching {
            if (tx.isInitialized()) {
                val connection = tx.connection
                connection.commit()
            }
        }.also {
            release(tx)
        }.onSuccess {
            loggerFacade.commit(tx.id)
        }.onFailure { cause ->
            runCatching {
                loggerFacade.commitFailed(tx.id, cause)
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.getOrThrow()
    }

    override fun suspend(): JdbcTransaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        loggerFacade.suspend(tx.id)
        return tx
    }

    override fun resume(tx: JdbcTransaction) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
        }
        threadLocal.set(tx)
        loggerFacade.resume(tx.id)
    }

    override fun rollback() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            return
        }
        rollbackInternal(tx)
    }

    /**
     * This function must not throw any exceptions.
     */
    private fun rollbackInternal(tx: JdbcTransaction) {
        runCatching {
            if (tx.isInitialized()) {
                val connection = tx.connection
                connection.rollback()
            }
        }.also {
            release(tx)
        }.onSuccess {
            runCatching {
                loggerFacade.rollback(tx.id)
            }
        }.onFailure { cause ->
            runCatching {
                loggerFacade.rollbackFailed(tx.id, cause)
            }
        }
    }

    /**
     * This function must not throw any exceptions.
     */
    private fun release(tx: JdbcTransaction) {
        threadLocal.remove()
        if (tx.isInitialized()) {
            val connection = tx.connection
            runCatching {
                connection.reset()
            }
            runCatching {
                connection.dispose()
            }
        }
    }
}

private fun JdbcTransaction?.isActive(): Boolean {
    return this != null
}
