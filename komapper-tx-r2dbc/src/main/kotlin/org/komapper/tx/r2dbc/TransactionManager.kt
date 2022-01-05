package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher
import kotlin.coroutines.CoroutineContext

/**
 * The R2DBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface TransactionManager {
    val connectionFactory: ConnectionFactory
    val isActive: Boolean
    val isRollbackOnly: Boolean
    fun setRollbackOnly()
    suspend fun begin(isolationLevel: IsolationLevel? = null): CoroutineContext
    suspend fun commit()
    suspend fun suspend(): Transaction
    suspend fun resume(tx: Transaction)
    suspend fun rollback()
}

internal class TransactionManagerImpl(
    private val internalConnectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade
) : TransactionManager {
    private val threadLocal: ThreadLocal<Transaction> = ThreadLocal()
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

    override suspend fun begin(isolationLevel: IsolationLevel?): CoroutineContext {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = internalConnectionFactory.create().awaitFirst().let { con ->
            val txCon = TransactionConnectionImpl(con, isolationLevel).apply {
                initialize()
            }
            TransactionImpl(txCon)
        }
        tx.connection.beginTransaction().awaitFirstOrNull()
        loggerFacade.begin(tx.id)
        return threadLocal.asContextElement(tx)
    }

    override suspend fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        val connection = tx.connection
        try {
            connection.commitTransaction().awaitFirstOrNull()
            loggerFacade.commit(tx.id)
        } catch (e: Exception) {
            rollbackInternal(tx)
            throw e
        } finally {
            release(tx)
        }
    }

    override suspend fun suspend(): Transaction {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        threadLocal.remove()
        return tx
    }

    override suspend fun resume(tx: Transaction) {
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

    private suspend fun rollbackInternal(tx: Transaction) {
        val connection = tx.connection
        try {
            connection.rollbackTransaction().awaitFirstOrNull()
            loggerFacade.rollback(tx.id)
        } catch (ignored: Exception) {
        } finally {
            release(tx)
        }
    }

    private suspend fun release(tx: Transaction) {
        threadLocal.remove()
        val connection = tx.connection
        try {
            connection.reset()
        } catch (ignored: Exception) {
        }
        connection.dispose()
    }
}

private fun Transaction?.isActive(): Boolean {
    return this != null
}
