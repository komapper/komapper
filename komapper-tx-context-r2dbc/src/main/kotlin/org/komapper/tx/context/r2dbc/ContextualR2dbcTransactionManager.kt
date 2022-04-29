package org.komapper.tx.context.r2dbc

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
import org.komapper.tx.r2dbc.R2dbcTransaction
import org.komapper.tx.r2dbc.R2dbcTransactionConnection
import org.komapper.tx.r2dbc.asDefinition

/**
 * The R2DBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface ContextualR2dbcTransactionManager {

    context(R2dbcTransactionContext)
    suspend fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    context(R2dbcTransactionContext)
    suspend fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(R2dbcTransactionContext)
    suspend fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(R2dbcTransactionContext)
    suspend fun setRollbackOnly()

    context(R2dbcTransactionContext)
    suspend fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty): R2dbcTransactionContext

    context(R2dbcTransactionContext)
    suspend fun commit()

    context(R2dbcTransactionContext)
    suspend fun suspend(): R2dbcTransactionContext

    context(R2dbcTransactionContext)
    suspend fun resume()

    /**
     * This function must not throw any exceptions.
     */
    context(R2dbcTransactionContext)
    suspend fun rollback()
}

internal class ContextualR2dbcTransactionManagerImpl(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade
) : ContextualR2dbcTransactionManager {

    context(R2dbcTransactionContext)
    override suspend fun getConnection(): Connection {
        val tx = transaction
        return tx?.connection ?: connectionFactory.create().asFlow().single()
    }

    context(R2dbcTransactionContext)
    override suspend fun isActive(): Boolean {
        val tx = transaction
        return tx != null
    }

    context(R2dbcTransactionContext)
    override suspend fun isRollbackOnly(): Boolean {
        val tx = transaction
        return tx?.isRollbackOnly ?: false
    }

    context(R2dbcTransactionContext)
    override suspend fun setRollbackOnly() {
        val tx = transaction
        if (tx != null) {
            tx.isRollbackOnly = true
        }
    }

    context(R2dbcTransactionContext)
    override suspend fun begin(transactionProperty: TransactionProperty): R2dbcTransactionContext {
        val currentTx = transaction
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
        return R2dbcTransactionContext(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun commit() {
        val tx = transaction ?: error("A transaction hasn't yet begun.")
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

    context(R2dbcTransactionContext)
    override suspend fun suspend(): R2dbcTransactionContext {
        val tx = transaction ?: error("A transaction hasn't yet begun.")
        loggerFacade.suspend(tx.toString())
        return EmptyR2dbcTransactionContext
    }

    context(R2dbcTransactionContext)
    override suspend fun resume() {
        val tx = transaction ?: error("A transaction is not found.")
        loggerFacade.resume(tx.toString())
    }

    context(R2dbcTransactionContext)
    override suspend fun rollback() {
        val tx = transaction ?: return
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
