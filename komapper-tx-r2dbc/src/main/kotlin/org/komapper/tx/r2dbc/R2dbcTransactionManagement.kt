package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

@ThreadSafe
interface R2dbcTransactionManagement {
    suspend fun getConnection(tx: R2dbcTransaction?): Connection

    suspend fun isActive(tx: R2dbcTransaction?): Boolean

    suspend fun isRollbackOnly(tx: R2dbcTransaction?): Boolean

    suspend fun setRollbackOnly(tx: R2dbcTransaction?)

    suspend fun begin(currentTx: R2dbcTransaction?, transactionProperty: TransactionProperty): R2dbcTransaction

    suspend fun commit(tx: R2dbcTransaction?)

    suspend fun suspend(tx: R2dbcTransaction?)

    suspend fun resume(tx: R2dbcTransaction?)

    suspend fun rollback(tx: R2dbcTransaction?)
}

private class R2dbcTransactionManagementImpl(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade,
) : R2dbcTransactionManagement {
    override suspend fun getConnection(tx: R2dbcTransaction?): Connection {
        return tx?.connection ?: connectionFactory.create().asFlow().single()
    }

    override suspend fun isActive(tx: R2dbcTransaction?): Boolean {
        return tx != null
    }

    override suspend fun isRollbackOnly(tx: R2dbcTransaction?): Boolean {
        return tx?.isRollbackOnly ?: false
    }

    override suspend fun setRollbackOnly(tx: R2dbcTransaction?) {
        if (tx != null) {
            tx.isRollbackOnly = true
        }
    }

    override suspend fun begin(
        currentTx: R2dbcTransaction?,
        transactionProperty: TransactionProperty,
    ): R2dbcTransaction {
        if (currentTx != null) {
            rollbackInternal(currentTx)
            error("The transaction \"${currentTx}\" already has begun.")
        }
        val tx = connectionFactory.create().asFlow().map { con ->
            val txCon = R2dbcTransactionConnection(con)
            val name = transactionProperty[TransactionProperty.Name]
            R2dbcTransaction(name?.value, txCon)
        }.single()
        val begin = if (transactionProperty == EmptyTransactionProperty) {
            tx.connection.beginTransaction().asFlow()
        } else {
            val definition = transactionProperty.asDefinition()
            tx.connection.beginTransaction(definition).asFlow()
        }
        begin.onCompletion { cause ->
            if (cause == null) {
                runCatching {
                    loggerFacade.begin(tx.toString())
                }.onFailure {
                    release(tx)
                }.getOrThrow()
            } else {
                release(tx)
            }
        }.collect()
        return tx
    }

    override suspend fun commit(tx: R2dbcTransaction?) {
        if (tx == null) error("A transaction hasn't yet begun.")
        val connection = tx.connection
        connection.commitTransaction().asFlow()
            .onCompletion { cause ->
                release(tx)
                if (cause == null) {
                    loggerFacade.commit(tx.toString())
                } else {
                    runCatching {
                        loggerFacade.commitFailed(tx.toString(), cause)
                    }.onFailure {
                        cause.addSuppressed(it)
                    }
                }
            }.collect()
    }

    override suspend fun suspend(tx: R2dbcTransaction?) {
        if (tx == null) error("A transaction hasn't yet begun.")
        loggerFacade.suspend(tx.toString())
    }

    override suspend fun resume(tx: R2dbcTransaction?) {
        if (tx == null) error("A transaction is not found.")
        loggerFacade.resume(tx.toString())
    }

    override suspend fun rollback(tx: R2dbcTransaction?) {
        if (tx == null) return
        rollbackInternal(tx)
    }

    /**
     * This function must not throw any exceptions.
     */
    private suspend fun rollbackInternal(tx: R2dbcTransaction) {
        val connection = tx.connection
        connection.rollbackTransaction().asFlow()
            .onCompletion { cause ->
                release(tx)
                runCatching {
                    if (cause == null) {
                        loggerFacade.rollback(tx.toString())
                    } else {
                        loggerFacade.rollbackFailed(tx.toString(), cause)
                    }
                }
            }.collect()
    }

    /**
     * This function must not throw any exceptions.
     */
    private suspend fun release(tx: R2dbcTransaction) {
        runCatching {
            tx.connection.dispose()
        }
    }
}

fun R2dbcTransactionManagement(connectionFactory: ConnectionFactory, loggerFacade: LoggerFacade): R2dbcTransactionManagement {
    return R2dbcTransactionManagementImpl(connectionFactory, loggerFacade)
}
