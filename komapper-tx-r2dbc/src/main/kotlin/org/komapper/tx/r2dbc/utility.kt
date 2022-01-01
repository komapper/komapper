package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.r2dbc.R2dbc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession

/**
 * Begins a R2DBC transaction.
 * @param R the return type of the block
 * @param transactionAttribute the transaction attribute
 * @param isolationLevel the isolation level. If null, the default isolation level is determined by the driver.
 * @param block the block executed in the transaction
 * @return the result of the block
 */
suspend fun <R> R2dbc.withTransaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: suspend TransactionScope.() -> R
): R {
    return if (this is R2dbcDatabase) {
        val session = this.config.session
        return if (session is TransactionSession) {
            session.userTransaction.withTransaction(transactionAttribute, isolationLevel, block)
        } else {
            withoutTransaction(block)
        }
    } else {
        withoutTransaction(block)
    }
}

private suspend fun <R> withoutTransaction(block: suspend TransactionScope.() -> R): R {
    val transactionScope = TransactionScopeStub()
    return block(transactionScope)
}

/**
 * The transaction manager.
 */
val R2dbcSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: R2dbcSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${TransactionSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
