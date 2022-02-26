package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.core.TransactionAttribute
import org.komapper.jdbc.JdbcIsolationLevel
import org.komapper.jdbc.JdbcTransactionDefinition
import org.komapper.jdbc.JdbcTransactionName
import org.komapper.jdbc.JdbcTransactionReadOnly

internal typealias JdbcDefinition = JdbcTransactionDefinition
internal typealias SpringDefinition = org.springframework.transaction.TransactionDefinition

internal fun adaptTransactionDefinition(
    adaptee: JdbcTransactionDefinition?,
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
        return adaptee[JdbcIsolationLevel]?.value ?: super.getIsolationLevel()
    }

    override fun isReadOnly(): Boolean {
        return adaptee[JdbcTransactionReadOnly]?.value ?: super.isReadOnly()
    }

    override fun getName(): String? {
        return adaptee[JdbcTransactionName]?.value ?: super.getName()
    }
}
