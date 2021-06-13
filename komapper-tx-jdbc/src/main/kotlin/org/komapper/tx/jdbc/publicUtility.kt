package org.komapper.tx.jdbc

import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseSession

private const val message = "DatabaseConfig.session must be an instance of TransactionDatabaseSession"

fun <R> JdbcDatabase.transaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: TransactionScope.() -> R
): R {
    val session = config.session
    return if (session is TransactionDatabaseSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        error(message)
    }
}

val JdbcDatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error(message)
        }
    }
