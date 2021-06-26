package org.komapper.tx.jdbc

import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseSession

fun <R> JdbcDatabase.withTransaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: TransactionScope.() -> R
): R {
    val session = config.session
    return if (session is TransactionDatabaseSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        invalidSession(session)
    }
}

val JdbcDatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: JdbcDatabaseSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${TransactionDatabaseSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
