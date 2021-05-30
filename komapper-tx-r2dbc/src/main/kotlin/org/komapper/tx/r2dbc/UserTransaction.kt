package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.core.ThreadSafe

@ThreadSafe
interface UserTransaction {

    suspend fun <R> transaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> required(isolationLevel, block)
            TransactionAttribute.REQUIRES_NEW -> requiresNew(isolationLevel, block)
        }
    }

    suspend fun <R> required(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R

    suspend fun <R> requiresNew(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R
}
