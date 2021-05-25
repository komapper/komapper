package org.komapper.tx.r2dbc

import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseSession

val R2dbcDatabase.transaction: UserTransaction
    get() {
        val session = config.session
        return if (session is TransactionDatabaseSession) {
            session.userTransaction
        } else {
            error("Check the session property of DatabaseConfig.")
        }
    }

val R2dbcDatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error("Check the session property of DatabaseConfig.")
        }
    }
