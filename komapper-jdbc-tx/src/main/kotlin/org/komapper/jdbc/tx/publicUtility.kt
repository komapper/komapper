package org.komapper.jdbc.tx

import org.komapper.jdbc.Database
import org.komapper.jdbc.DatabaseSession

val Database.transaction: UserTransaction
    get() {
        val session = config.session
        return if (session is TransactionDatabaseSession) {
            session.userTransaction
        } else {
            error("Check the session property of DatabaseConfig.")
        }
    }

val DatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error("Check the session property of DatabaseConfig.")
        }
    }
