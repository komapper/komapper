package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseSession

suspend fun <R> R2dbcDatabase.withTransaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: suspend TransactionScope.() -> R
): R {
    val session = config.session
    return if (session is TransactionDatabaseSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        invalidSession(session)
    }
}

val R2dbcDatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: R2dbcDatabaseSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${TransactionDatabaseSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
