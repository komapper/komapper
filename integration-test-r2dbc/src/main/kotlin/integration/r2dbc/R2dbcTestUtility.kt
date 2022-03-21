package integration.r2dbc

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.TestInfo
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.TransactionProperty

fun <T> inTransaction(db: R2dbcDatabase, info: TestInfo, block: suspend (CoroutineTransactionOperator) -> T) {
    runBlockingWithTimeout {
        // SQL Server does not allow transaction names to be keywords or longer than 33 characters
        val name = info.testMethod.map { it.name }.orElse("unknown").let { "komapper_$it" }.take(32)
        db.withTransaction(transactionProperty = TransactionProperty.Name(name)) {
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
