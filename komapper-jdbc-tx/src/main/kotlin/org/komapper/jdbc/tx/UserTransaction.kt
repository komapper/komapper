package org.komapper.jdbc.tx

import org.komapper.core.ThreadSafe

@ThreadSafe
interface UserTransaction {
    operator fun <R> invoke(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R = required(isolationLevel, block)

    fun <R> required(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R

    fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R
}
