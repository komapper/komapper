package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * The R2DBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface R2dbcTransactionManager {
    val connectionFactory: ConnectionFactory
    val isActive: Boolean
    val isRollbackOnly: Boolean

    /**
     * This function must not throw any exceptions.
     */
    fun setRollbackOnly()

    suspend fun begin(transactionDefinition: TransactionDefinition? = null): CoroutineContext

    suspend fun commit()

    suspend fun suspend(): CoroutineContext

    /**
     * This function must not throw any exceptions.
     */
    suspend fun rollback()
}

internal class R2dbcTransactionManagerImpl(
    private val internalConnectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade
) : R2dbcTransactionManager {
    private val threadLocal: ThreadLocal<R2dbcTransaction> = ThreadLocal.withInitial { Null }
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

    override suspend fun begin(transactionDefinition: TransactionDefinition?): CoroutineContext {
        val currentTx = threadLocal.get()
        if (currentTx.isActive()) {
            rollbackInternal(currentTx)
            error("The transaction \"$currentTx\" already has begun.")
        }
        val tx = internalConnectionFactory.create().asFlow().map { con ->
            val txCon = R2dbcTransactionConnectionImpl(con)
            R2dbcTransactionImpl(txCon)
        }.single()
        val begin = if (transactionDefinition == null) {
            tx.connection.beginTransaction().asFlow()
        } else {
            tx.connection.beginTransaction(transactionDefinition).asFlow()
        }
        begin.onCompletion { cause ->
            if (cause == null) {
                runCatching {
                    loggerFacade.begin(tx.id)
                }.onFailure {
                    release(tx)
                }.getOrThrow()
            } else {
                release(tx)
            }
        }.collect()
        return threadLocal.asContextElement(tx)
    }

    override suspend fun commit() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        val connection = tx.connection
        connection.commitTransaction().asFlow()
            .onCompletion { cause ->
                release(tx)
                if (cause == null) {
                    loggerFacade.commit(tx.id)
                } else {
                    runCatching {
                        loggerFacade.commitFailed(tx.id, cause)
                    }.onFailure {
                        cause.addSuppressed(it)
                    }
                }
            }.collect()
    }

    override suspend fun suspend(): CoroutineContext {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            error("A transaction hasn't yet begun.")
        }
        return threadLocal.asContextElement(Null)
    }

    override suspend fun rollback() {
        val tx = threadLocal.get()
        if (!tx.isActive()) {
            return
        }
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
                        loggerFacade.rollback(tx.id)
                    } else {
                        loggerFacade.rollbackFailed(tx.id, cause)
                    }
                }
            }
            .catch {
                // ignore
            }.collect()
    }

    /**
     * This function must not throw any exceptions.
     */
    private suspend fun release(tx: R2dbcTransaction) {
        threadLocal.remove()
        runCatching {
            tx.connection.dispose()
        }
    }
}

private fun R2dbcTransaction?.isActive(): Boolean {
    return this != Null
}

private object Null : R2dbcTransaction {
    override val id: UUID
        get() = throw UnsupportedOperationException()
    override val connection: R2dbcTransactionConnection
        get() = throw UnsupportedOperationException()
    override var isRollbackOnly: Boolean
        get() = throw UnsupportedOperationException()
        set(_) {
            throw UnsupportedOperationException()
        }
}
