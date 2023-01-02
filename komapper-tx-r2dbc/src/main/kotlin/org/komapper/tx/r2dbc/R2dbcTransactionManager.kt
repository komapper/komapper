package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * The R2DBC transaction APIs designed for advanced use.
 */
@ThreadSafe
interface R2dbcTransactionManager {

    suspend fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    suspend fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    suspend fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    suspend fun setRollbackOnly()

    suspend fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty): CoroutineContext

    suspend fun commit()

    suspend fun suspend(): CoroutineContext

    suspend fun resume()

    /**
     * This function must not throw any exceptions.
     */
    suspend fun rollback()
}

internal class R2dbcTransactionManagerImpl(
    connectionFactory: ConnectionFactory,
    loggerFacade: LoggerFacade,
) : R2dbcTransactionManager {

    private val management: R2dbcTransactionManagement = R2dbcTransactionManagement(connectionFactory, loggerFacade)

    override suspend fun getConnection(): Connection {
        val tx = coroutineContext[TxHolder]?.tx
        return management.getConnection(tx)
    }

    override suspend fun isActive(): Boolean {
        val tx = coroutineContext[TxHolder]?.tx
        return management.isActive(tx)
    }

    override suspend fun isRollbackOnly(): Boolean {
        val tx = coroutineContext[TxHolder]?.tx
        return management.isRollbackOnly(tx)
    }

    override suspend fun setRollbackOnly() {
        val tx = coroutineContext[TxHolder]?.tx
        management.setRollbackOnly(tx)
    }

    override suspend fun begin(transactionProperty: TransactionProperty): CoroutineContext {
        val currentTx = coroutineContext[TxHolder]?.tx
        val tx = management.begin(currentTx, transactionProperty)
        return TxHolder(tx)
    }

    override suspend fun commit() {
        val tx = coroutineContext[TxHolder]?.tx
        management.commit(tx)
    }

    override suspend fun suspend(): CoroutineContext {
        val tx = coroutineContext[TxHolder]?.tx
        management.suspend(tx)
        return TxHolder(null)
    }

    override suspend fun resume() {
        val tx = coroutineContext[TxHolder]?.tx
        management.resume(tx)
    }

    override suspend fun rollback() {
        val tx = coroutineContext[TxHolder]?.tx
        management.rollback(tx)
    }
}

private data class TxHolder(val tx: R2dbcTransaction?) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<TxHolder>

    override val key: CoroutineContext.Key<TxHolder> = Key
}
