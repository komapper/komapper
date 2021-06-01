package integration.r2dbc

import kotlinx.coroutines.runBlocking
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.TransactionScope
import org.komapper.tx.r2dbc.transaction

fun <T> inTransaction(db: R2dbcDatabase, block: suspend TransactionScope.() -> T) {
    runBlocking {
        db.transaction {
            setRollbackOnly()
            block()
        }
    }
}
