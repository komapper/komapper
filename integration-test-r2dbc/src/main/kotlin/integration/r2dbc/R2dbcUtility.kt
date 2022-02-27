package integration.r2dbc

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.komapper.r2dbc.R2dbcCoroutineTransactionOperator
import org.komapper.r2dbc.R2dbcDatabase

fun <T> inTransaction(db: R2dbcDatabase, block: suspend (R2dbcCoroutineTransactionOperator) -> T) {
    runBlockingWithTimeout {
        db.withTransaction {
            it.setRollbackOnly()
            block(it)
        }
    }
}

fun <T> runBlockingWithTimeout(timeMillis: Long = 30000L, block: suspend () -> T) {
    runBlocking {
        withTimeout(timeMillis) {
            block()
        }
    }
}
