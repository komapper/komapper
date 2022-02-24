package integration.r2dbc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.R2dbcUserTransaction
import org.komapper.tx.r2dbc.withTransaction

fun <T> inTransaction(db: R2dbcDatabase, block: suspend CoroutineScope.(R2dbcUserTransaction) -> T) {
    runBlockingWithTimeout {
        db.withTransaction {
            it.setRollbackOnly()
            block(it)
        }
    }
}

fun <T> runBlockingWithTimeout(timeMillis: Long = 30000L, block: suspend CoroutineScope.() -> T) {
    runBlocking {
        withTimeout(timeMillis, block)
    }
}
