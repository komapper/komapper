package integration.r2dbc

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.TestInfo
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.TransactionProperty

fun <T> inTransaction(db: R2dbcDatabase, info: TestInfo, block: suspend (CoroutineTransactionOperator) -> T) {
    runBlockingWithTimeout {
        val name = TransactionProperty.Name(info.displayName)
        db.withTransaction(transactionProperty = name) {
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
