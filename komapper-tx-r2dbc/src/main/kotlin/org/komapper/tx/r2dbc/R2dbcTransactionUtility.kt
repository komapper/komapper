package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.CoroutineScope
import org.komapper.r2dbc.R2dbc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession
import kotlin.coroutines.coroutineContext

/**
 * Begins a R2DBC transaction.
 *
 * @param R the return type of the block
 * @param transactionAttribute the transaction attribute
 * @param transactionDefinition the transactionDefinition level
 * @param block the block executed in the transaction
 * @return the result of the block
 */
suspend fun <R> R2dbc.withTransaction(
    transactionAttribute: R2dbcTransactionAttribute = R2dbcTransactionAttribute.REQUIRED,
    transactionDefinition: TransactionDefinition? = null,
    block: suspend CoroutineScope.(R2dbcUserTransaction) -> R
): R {
    return if (this is R2dbcDatabase) {
        val session = this.config.session
        return if (session is R2dbcTransactionSession) {
            session.userTransaction.run(transactionAttribute, transactionDefinition, block)
        } else {
            withoutTransaction(block)
        }
    } else {
        withoutTransaction(block)
    }
}

private suspend fun <R> withoutTransaction(block: suspend CoroutineScope.(R2dbcUserTransaction) -> R): R {
    val userTransaction = R2dbcUserTransactionStub()
    return block(CoroutineScope(coroutineContext), userTransaction)
}

/**
 * The transaction manager.
 */
@Suppress("unused")
val R2dbcSession.transactionManager: R2dbcTransactionManager
    get() {
        return if (this is R2dbcTransactionSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: R2dbcSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${R2dbcTransactionSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
