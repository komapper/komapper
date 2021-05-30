package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseSession

private const val message = "R2dbcDatabaseConfig.session must be an instance of TransactionDatabaseSession"

suspend fun <R> R2dbcDatabase.transaction(
    transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
    isolationLevel: IsolationLevel? = null,
    block: suspend TransactionScope.() -> R
): R {
    val session = config.session
    return if (session is TransactionDatabaseSession) {
        session.userTransaction.transaction(transactionAttribute, isolationLevel, block)
    } else {
        error(message)
    }
}

val R2dbcDatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error(message)
        }
    }
