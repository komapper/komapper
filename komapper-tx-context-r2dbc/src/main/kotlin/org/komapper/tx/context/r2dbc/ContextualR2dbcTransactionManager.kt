package org.komapper.tx.context.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty
import org.komapper.tx.r2dbc.R2dbcTransactionManagement

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
    connectionFactory: ConnectionFactory,
    loggerFacade: LoggerFacade
) : ContextualR2dbcTransactionManager {

    private val management: R2dbcTransactionManagement =
        R2dbcTransactionManagement(connectionFactory, loggerFacade)

    context(R2dbcTransactionContext)
    override suspend fun getConnection(): Connection {
        val tx = transaction
        return management.getConnection(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun isActive(): Boolean {
        val tx = transaction
        return management.isActive(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun isRollbackOnly(): Boolean {
        val tx = transaction
        return management.isRollbackOnly(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun setRollbackOnly() {
        val tx = transaction
        management.setRollbackOnly(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun begin(transactionProperty: TransactionProperty): R2dbcTransactionContext {
        val currentTx = transaction
        val tx = management.begin(currentTx, transactionProperty)
        return R2dbcTransactionContext(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun commit() {
        val tx = transaction
        management.commit(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun suspend(): R2dbcTransactionContext {
        val tx = transaction
        management.suspend(tx)
        return EmptyR2dbcTransactionContext
    }

    context(R2dbcTransactionContext)
    override suspend fun resume() {
        val tx = transaction
        management.resume(tx)
    }

    context(R2dbcTransactionContext)
    override suspend fun rollback() {
        val tx = transaction
        management.rollback(tx)
    }
}
