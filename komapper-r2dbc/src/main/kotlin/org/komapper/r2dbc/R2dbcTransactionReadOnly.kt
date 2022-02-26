package org.komapper.r2dbc

import io.r2dbc.spi.Option
import io.r2dbc.spi.TransactionDefinition

data class R2dbcTransactionReadOnly(val value: Boolean) : TransactionDefinition {
    override fun <T : Any> getAttribute(option: Option<T>): T? {
        if (option == TransactionDefinition.READ_ONLY) {
            return option.cast(value)
        }
        return null
    }
}
