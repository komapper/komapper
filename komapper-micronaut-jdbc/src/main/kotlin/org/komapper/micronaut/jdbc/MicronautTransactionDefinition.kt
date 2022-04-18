package org.komapper.micronaut.jdbc

import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.TransactionDefinition.Isolation
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty

internal class MicronautTransactionDefinition(
    private val transactionProperty: TransactionProperty,
    private val transactionAttribute: TransactionAttribute
) : TransactionDefinition {

    override fun getPropagationBehavior(): TransactionDefinition.Propagation {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> TransactionDefinition.Propagation.REQUIRED
            TransactionAttribute.REQUIRES_NEW -> TransactionDefinition.Propagation.REQUIRES_NEW
        }
    }

    override fun getIsolationLevel(): Isolation {
        return when (transactionProperty[TransactionProperty.IsolationLevel]) {
            TransactionProperty.IsolationLevel.READ_UNCOMMITTED -> Isolation.READ_UNCOMMITTED
            TransactionProperty.IsolationLevel.READ_COMMITTED -> Isolation.READ_COMMITTED
            TransactionProperty.IsolationLevel.REPEATABLE_READ -> Isolation.REPEATABLE_READ
            TransactionProperty.IsolationLevel.SERIALIZABLE -> Isolation.SERIALIZABLE
            else -> super.getIsolationLevel()
        }
    }

    override fun isReadOnly(): Boolean {
        return transactionProperty[TransactionProperty.ReadOnly]?.value ?: super.isReadOnly()
    }

    override fun getName(): String? {
        return transactionProperty[TransactionProperty.Name]?.value ?: super.getName()
    }
}
