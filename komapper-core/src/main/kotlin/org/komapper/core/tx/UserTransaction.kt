package org.komapper.core.tx

interface UserTransaction {
    operator fun <R> invoke(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R

    fun <R> required(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R

    fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R
}
