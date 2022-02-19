package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher

/**
 * The R2DBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface TransactionManager {
    val connectionFactory: ConnectionFactory
    val isActive: Boolean
    val isRollbackOnly: Boolean

    /**
     * This function must not throw an exception.
     */
    fun setRollbackOnly()

    suspend fun <R> begin(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend CoroutineScope.() -> R
    ): R

    suspend fun commit()

    suspend fun suspend(): R2dbcTransaction

    /**
     * This function must not throw an exception.
     */
    suspend fun resume(tx: R2dbcTransaction)

    /**
     * This function must not throw an exception.
     */
    suspend fun rollback()
}

internal class TransactionManagerImpl(
    private val internalConnectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade
) : TransactionManager {
    private val threadLocal: ThreadLocal<R2dbcTransaction> = ThreadLocal()
    override val connectionFactory: ConnectionFactory = object : ConnectionFactory {
        override fun create(): Publisher<out Connection> {
            val tx = threadLocal.get()
            if (!tx.isActive()) {
                error("A transaction hasn't yet begun.")
            }
            return flowOf(tx.connection).asPublisher()
        }

        override fun getMetadata(): ConnectionFactoryMetadata {
            return internalConnectionFactory.metadata
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

    override suspend fun <R> begin(
        transactionDefinition: TransactionDefinition?,
        block: suspend CoroutineScope.() -> R
    ): R {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = internalConnectionFactory.create().awaitSingle().let { con ->
            val txCon = R2dbcTransactionConnectionImpl(con)
            R2dbcTransactionImpl(txCon)
        }
        runCatching {
            if (transactionDefinition == null) {
                tx.connection.beginTransaction().awaitFirstOrNull()
            } else {
                tx.connection.beginTransaction(transactionDefinition).awaitFirstOrNull()
            }
        }.onFailure { cause ->
            runCatching {
                tx.connection.dispose()
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.getOrThrow()
        loggerFacade.begin(tx.id)
        val context = threadLocal.asContextElement(tx)
        return withContext(context, block)
    }

    override suspend fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        val connection = tx.connection
        val result = runCatching {
            connection.commitTransaction().awaitFirstOrNull()
        }.onFailure { cause ->
            runCatching {
                loggerFacade.commitFailed(tx.id, cause)
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.onSuccess {
            loggerFacade.commit(tx.id)
        }
        rollbackInternal(tx)
        result.getOrThrow()
    }

    override suspend fun suspend(): R2dbcTransaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        return tx
    }

    override suspend fun resume(tx: R2dbcTransaction) {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
        }
        threadLocal.set(tx)
    }

    override suspend fun rollback() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            return
        }
        rollbackInternal(tx)
    }

    /**
     * This function must not throw an exception.
     */
    private suspend fun rollbackInternal(tx: R2dbcTransaction) {
        val connection = tx.connection
        runCatching {
            connection.rollbackTransaction().awaitFirstOrNull()
        }.onFailure {
            runCatching {
                loggerFacade.rollbackFailed(tx.id, it)
            }
        }.onSuccess {
            runCatching {
                loggerFacade.rollback(tx.id)
            }
        }
        release(tx)
    }

    /**
     * This function must not throw an exception.
     */
    private suspend fun release(tx: R2dbcTransaction) {
        threadLocal.remove()
        runCatching {
            tx.connection.dispose()
        }
    }
}

private fun R2dbcTransaction?.isActive(): Boolean {
    return this != null
}
