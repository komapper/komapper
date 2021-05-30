package org.komapper.tx.jdbc

import org.komapper.jdbc.Database
import org.komapper.jdbc.DatabaseSession

private const val message = "DatabaseConfig.session must be an instance of TransactionDatabaseSession"

fun <R> Database.transaction(
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

val DatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error(message)
        }
    }
