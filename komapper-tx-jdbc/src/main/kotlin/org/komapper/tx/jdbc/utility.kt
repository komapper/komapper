package org.komapper.tx.jdbc

import org.komapper.jdbc.Jdbc
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcSession

fun <R> Jdbc.withTransaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: TransactionScope.() -> R
): R {
    val database = this as JdbcDatabase
    val session = database.config.session
    return if (session is TransactionSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        invalidSession(session)
    }
}

val JdbcSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: JdbcSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${TransactionSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
