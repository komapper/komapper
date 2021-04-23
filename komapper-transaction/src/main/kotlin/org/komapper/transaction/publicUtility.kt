package org.komapper.transaction

import org.komapper.core.Database
import org.komapper.core.DatabaseSession

val Database.transaction: UserTransaction
    get() {
        val session = config.session
        return if (session is TransactionDatabaseSession) {
            session.userTransaction
        } else {
            error("Enable transaction.")
        }
    }

val DatabaseSession.transactionManager: TransactionManager
    get() {
        return if (this is TransactionDatabaseSession) {
            this.transactionManager
        } else {
            error("Enable transaction.")
        }
    }
