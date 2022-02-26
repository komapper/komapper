package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.core.TransactionAttribute
import org.komapper.jdbc.JdbcIsolationLevel

internal typealias JdbcDefinition = JdbcIsolationLevel
internal typealias SpringDefinition = org.springframework.transaction.TransactionDefinition

internal fun adaptTransactionDefinition(
    adaptee: JdbcDefinition?,
    transactionAttribute: TransactionAttribute
): SpringDefinition {
    val adapter = if (adaptee == null) {
        SpringDefinition.withDefaults()
    } else {
        JdbcTransactionDefinitionAdapter(adaptee)
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

private class JdbcTransactionDefinitionAdapter(
    private val adaptee: JdbcDefinition
) :
    SpringDefinition {

    override fun getIsolationLevel(): Int {
        return adaptee.value
    }

    override fun isReadOnly(): Boolean {
        return super.isReadOnly()
    }
}
