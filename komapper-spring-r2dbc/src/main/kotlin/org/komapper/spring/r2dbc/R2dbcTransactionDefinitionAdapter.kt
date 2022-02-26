package org.komapper.spring.r2dbc

import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.TransactionAttribute
import org.komapper.r2dbc.get

internal typealias R2dbcDefinition = TransactionDefinition
internal typealias SpringDefinition = org.springframework.transaction.TransactionDefinition

internal fun adaptTransactionDefinition(
    adaptee: R2dbcDefinition?,
    transactionAttribute: TransactionAttribute
): SpringDefinition {
    val adapter = if (adaptee == null) {
        SpringDefinition.withDefaults()
    } else {
        R2dbcTransactionDefinitionAdapter(adaptee)
    }
    return object : SpringDefinition by adapter {
        override fun getPropagationBehavior(): Int {
            return when (transactionAttribute) {
                TransactionAttribute.REQUIRED -> SpringDefinition.PROPAGATION_REQUIRED
                TransactionAttribute.REQUIRES_NEW -> SpringDefinition.PROPAGATION_REQUIRES_NEW
            }
        }
    }
}

private class R2dbcTransactionDefinitionAdapter(
    private val adaptee: R2dbcDefinition
) :
    SpringDefinition {

    override fun getIsolationLevel(): Int {
        val value = adaptee[R2dbcDefinition.ISOLATION_LEVEL]
        return if (value != null) {
            when (value) {
                IsolationLevel.READ_UNCOMMITTED -> SpringDefinition.ISOLATION_READ_UNCOMMITTED
                IsolationLevel.READ_COMMITTED -> SpringDefinition.ISOLATION_READ_COMMITTED
                IsolationLevel.REPEATABLE_READ -> SpringDefinition.ISOLATION_REPEATABLE_READ
                IsolationLevel.SERIALIZABLE -> SpringDefinition.ISOLATION_SERIALIZABLE
                else -> error("unknown isolation level: $value")
            }
        } else {
            super.getIsolationLevel()
        }
    }

    override fun isReadOnly(): Boolean {
        return adaptee[R2dbcDefinition.READ_ONLY] ?: super.isReadOnly()
    }

    override fun getName(): String? {
        return adaptee[R2dbcDefinition.NAME] ?: super.getName()
    }
}
