package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.withContext
import org.komapper.core.ThreadSafe

/**
 * The R2DBC transaction APIs designed to be used in general cases.
 * If the isolationLevel null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface R2dbcUserTransaction {

    /**
     * Runs a transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> run(
        transactionAttribute: R2dbcTransactionAttribute = R2dbcTransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        return when (transactionAttribute) {
            R2dbcTransactionAttribute.REQUIRED -> required(transactionDefinition, block)
            R2dbcTransactionAttribute.REQUIRES_NEW -> requiresNew(transactionDefinition, block)
        }
    }

    /**
     * Begins a transaction with [R2dbcTransactionAttribute.REQUIRED].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend (R2dbcUserTransaction) -> R
    ): R

    /**
     * Begins a transaction with [R2dbcTransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend (R2dbcUserTransaction) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class R2dbcUserTransactionImpl(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : R2dbcUserTransaction {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        return if (transactionManager.isActive) {
            val txContext = transactionManager.suspend()
            withContext(txContext) {
                executeInNewTransaction(transactionDefinition, block)
            }
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        val txContext = transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition)
        return withContext(txContext) {
            runCatching {
                block(this@R2dbcUserTransactionImpl)
            }.onSuccess {
                if (transactionManager.isRollbackOnly) {
                    transactionManager.rollback()
                } else {
                    transactionManager.commit()
                }
            }.onFailure { cause ->
                runCatching {
                    transactionManager.rollback()
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }.getOrThrow()
        }
    }

    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly
    }
}

internal class R2dbcUserTransactionStub : R2dbcUserTransaction {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        return block(this)
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcUserTransaction) -> R
    ): R {
        return block(this)
    }
}
