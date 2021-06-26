package integration.r2dbc

import kotlinx.coroutines.runBlocking
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.TransactionScope
import org.komapper.tx.r2dbc.withTransaction

fun <T> inTransaction(db: R2dbcDatabase, block: suspend TransactionScope.() -> T) {
    runBlocking {
        db.withTransaction {
            setRollbackOnly()
            block()
        }
    }
}
