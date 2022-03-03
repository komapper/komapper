package org.komapper.spring.jdbc

import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.TransactionDefinition

internal class PlatformTransactionDefinition(
    private val transactionProperty: TransactionProperty,
    private val transactionAttribute: TransactionAttribute
) : TransactionDefinition by TransactionDefinition.withDefaults() {

    override fun getPropagationBehavior(): Int {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> TransactionDefinition.PROPAGATION_REQUIRED
            TransactionAttribute.REQUIRES_NEW -> TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }
    }

    override fun getIsolationLevel(): Int {
        return transactionProperty[TransactionProperty.IsolationLevel]?.value ?: super.getIsolationLevel()
    }

    override fun isReadOnly(): Boolean {
        return transactionProperty[TransactionProperty.ReadOnly]?.value ?: super.isReadOnly()
    }

    override fun getName(): String? {
        return transactionProperty[TransactionProperty.Name]?.value ?: super.getName()
    }
}
