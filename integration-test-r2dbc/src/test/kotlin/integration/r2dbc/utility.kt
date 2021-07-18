package integration.r2dbc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.TransactionScope
import org.komapper.tx.r2dbc.withTransaction

fun <T> inTransaction(db: R2dbcDatabase, block: suspend TransactionScope.() -> T) {
    runBlockingWithTimeout {
        db.withTransaction {
            setRollbackOnly()
            block()
        }
    }
}

fun <T> runBlockingWithTimeout(timeMillis: Long = 30000L, block: suspend CoroutineScope.() -> T) {
    runBlocking {
        withTimeout(timeMillis, block)
    }
}
