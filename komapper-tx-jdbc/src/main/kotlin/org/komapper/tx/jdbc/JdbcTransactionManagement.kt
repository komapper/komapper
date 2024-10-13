package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.TransactionProperty
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface JdbcTransactionManagement {
    fun getConnection(tx: JdbcTransaction?): Connection

    fun isActive(tx: JdbcTransaction?): Boolean

    fun isRollbackOnly(tx: JdbcTransaction?): Boolean

    fun setRollbackOnly(tx: JdbcTransaction?)

    fun begin(currentTx: JdbcTransaction?, transactionProperty: TransactionProperty): JdbcTransaction

    fun commit(tx: JdbcTransaction?)

    fun suspend(tx: JdbcTransaction?)

    fun resume(tx: JdbcTransaction?)

    fun rollback(tx: JdbcTransaction?)
}

private class JdbcTransactionManagementImpl(
    private val dataSource: DataSource,
    private val loggerFacade: LoggerFacade,
    private val releaseAction: JdbcTransactionReleaseAction,
) : JdbcTransactionManagement {
    override fun getConnection(tx: JdbcTransaction?): Connection {
        return tx?.connection ?: dataSource.connection
    }

    override fun isActive(tx: JdbcTransaction?): Boolean {
        return tx != null
    }

    override fun isRollbackOnly(tx: JdbcTransaction?): Boolean {
        return tx?.isRollbackOnly ?: false
    }

    override fun setRollbackOnly(tx: JdbcTransaction?) {
        if (tx != null) {
            tx.isRollbackOnly = true
        }
    }

    override fun begin(currentTx: JdbcTransaction?, transactionProperty: TransactionProperty): JdbcTransaction {
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
        return tx
    }

    override fun commit(tx: JdbcTransaction?) {
        if (tx == null) error("A transaction hasn't yet begun.")
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

    override fun suspend(tx: JdbcTransaction?) {
        if (tx == null) error("A transaction hasn't yet begun.")
        loggerFacade.suspend(tx.toString())
    }

    override fun resume(tx: JdbcTransaction?) {
        if (tx == null) error("A transaction is not found.")
        loggerFacade.resume(tx.toString())
    }

    override fun rollback(tx: JdbcTransaction?) {
        if (tx == null) return
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
        releaseAction.execute()
        runCatching {
            tx.connection.reset()
        }
        runCatching {
            tx.connection.dispose()
        }
    }
}

fun JdbcTransactionManagement(
    dataSource: DataSource,
    loggerFacade: LoggerFacade,
    releaseAction: JdbcTransactionReleaseAction = JdbcTransactionReleaseAction { },
): JdbcTransactionManagement {
    return JdbcTransactionManagementImpl(dataSource, loggerFacade, releaseAction)
}
