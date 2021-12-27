package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.r2dbc.R2dbc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession

suspend fun <R> R2dbc.withTransaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: suspend TransactionScope.() -> R
): R {
    val database = this as R2dbcDatabase
    val session = database.config.session
    return if (session is TransactionSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        invalidSession(session)
    }
}

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
