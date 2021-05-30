package org.komapper.tx.jdbc

import org.komapper.core.ThreadSafe

@ThreadSafe
interface UserTransaction {

    fun <R> transaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        isolationLevel: IsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> required(isolationLevel, block)
            TransactionAttribute.REQUIRES_NEW -> requiresNew(isolationLevel, block)
        }
    }

    fun <R> required(
        isolationLevel: IsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R

    fun <R> requiresNew(
        isolationLevel: IsolationLevel? = null,
        block: TransactionScope.() -> R
    ): R
}
