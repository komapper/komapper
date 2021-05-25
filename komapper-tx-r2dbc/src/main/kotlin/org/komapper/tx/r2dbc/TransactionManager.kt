package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.R2dbcException
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.komapper.core.LogCategory
import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher
import kotlin.coroutines.CoroutineContext

@ThreadSafe
interface TransactionManager {
    val connectionFactory: ConnectionFactory
    val isActive: Boolean
    val isRollbackOnly: Boolean
    fun setRollbackOnly()
    fun createEmptyContext(): CoroutineContext
    suspend fun begin(isolationLevel: IsolationLevel? = null): CoroutineContext
    suspend fun commit()
    suspend fun rollback()
}

internal class TransactionManagerImpl(
    private val internalConnectionFactory: ConnectionFactory,
    private val logger: Logger
) : TransactionManager {
    private val threadLocal: ThreadLocal<Transaction> = ThreadLocal.withInitial { EmptyTransaction }
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

    override fun createEmptyContext(): CoroutineContext {
        return threadLocal.asContextElement(EmptyTransaction)
    }

    override suspend fun begin(isolationLevel: IsolationLevel?): CoroutineContext {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = internalConnectionFactory.create().awaitFirst().let {
            TransactionImpl(TransactionConnectionImpl(it, isolationLevel))
        }
        tx.connection.beginTransaction().awaitFirstOrNull()
        logger.trace(LogCategory.TRANSACTION.value) { "The transaction \"$tx\" has begun." }
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
            logger.trace(LogCategory.TRANSACTION.value) { "The transaction \"$tx\" has committed." }
        } catch (e: Exception) {
            rollbackInternal(tx)
            throw e
        } finally {
            release(tx)
        }
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
            logger.trace(LogCategory.TRANSACTION.value) { "The transaction \"$tx\" has rolled back." }
        } catch (ignored: R2dbcException) {
        } finally {
            release(tx)
        }
    }

    private suspend fun release(tx: Transaction) {
        threadLocal.remove()
        val connection = tx.connection
        try {
            connection.reset()
        } catch (ignored: R2dbcException) {
        }
        connection.close().awaitFirstOrNull()
    }

    private fun Transaction.isActive(): Boolean {
        return this != EmptyTransaction
    }
}
