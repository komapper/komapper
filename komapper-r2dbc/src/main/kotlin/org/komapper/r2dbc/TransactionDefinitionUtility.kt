package org.komapper.r2dbc

import io.r2dbc.spi.Option
import io.r2dbc.spi.TransactionDefinition

infix operator fun <T : Any> TransactionDefinition.get(option: Option<T>): T? {
    return this.getAttribute(option)
}

infix operator fun TransactionDefinition.plus(other: TransactionDefinition): TransactionDefinition {
    return CombinedTransactionDefinition(this, other)
}

private class CombinedTransactionDefinition(val left: TransactionDefinition, val right: TransactionDefinition) :
    TransactionDefinition {
    override fun <T : Any> getAttribute(option: Option<T>): T? {
        val attribute = right.getAttribute(option)
        if (attribute != null) {
            return attribute
        }
        return left.getAttribute(option)
    }
}
