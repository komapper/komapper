package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.core.ThreadSafe

@ThreadSafe
interface UserTransaction {
    suspend operator fun <R> invoke(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R = required(isolationLevel, block)

    suspend fun <R> required(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R

    suspend fun <R> requiresNew(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R
}
