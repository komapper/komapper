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
    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun getConnection(): Connection

    /**
     * This function must not throw any exceptions.
     */
    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun isActive(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun isRollbackOnly(): Boolean

    /**
     * This function must not throw any exceptions.
     */
    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun setRollbackOnly()

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun begin(transactionProperty: TransactionProperty = EmptyTransactionProperty): R2dbcTransactionContext

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun commit()

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun suspend(): R2dbcTransactionContext

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun resume()

    /**
     * This function must not throw any exceptions.
     */
    context(r2dbcTransactionContext: R2dbcTransactionContext)
    suspend fun rollback()
}

internal class ContextualR2dbcTransactionManagerImpl(
    connectionFactory: ConnectionFactory,
    loggerFacade: LoggerFacade,
) : ContextualR2dbcTransactionManager {
    private val management: R2dbcTransactionManagement =
        R2dbcTransactionManagement(connectionFactory, loggerFacade)

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun getConnection(): Connection {
        val tx = r2dbcTransactionContext.transaction
        return management.getConnection(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun isActive(): Boolean {
        val tx = r2dbcTransactionContext.transaction
        return management.isActive(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun isRollbackOnly(): Boolean {
        val tx = r2dbcTransactionContext.transaction
        return management.isRollbackOnly(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun setRollbackOnly() {
        val tx = r2dbcTransactionContext.transaction
        management.setRollbackOnly(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun begin(transactionProperty: TransactionProperty): R2dbcTransactionContext {
        val currentTx = r2dbcTransactionContext.transaction
        val tx = management.begin(currentTx, transactionProperty)
        return R2dbcTransactionContext(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun commit() {
        val tx = r2dbcTransactionContext.transaction
        management.commit(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun suspend(): R2dbcTransactionContext {
        val tx = r2dbcTransactionContext.transaction
        management.suspend(tx)
        return EmptyR2dbcTransactionContext
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun resume() {
        val tx = r2dbcTransactionContext.transaction
        management.resume(tx)
    }

    context(r2dbcTransactionContext: R2dbcTransactionContext)
    override suspend fun rollback() {
        val tx = r2dbcTransactionContext.transaction
        management.rollback(tx)
    }
}
