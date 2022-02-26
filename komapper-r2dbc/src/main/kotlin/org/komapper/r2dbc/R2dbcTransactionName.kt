package org.komapper.r2dbc

import io.r2dbc.spi.Option
import io.r2dbc.spi.TransactionDefinition

data class R2dbcTransactionName(val value: String) : TransactionDefinition {
    override fun <T : Any> getAttribute(option: Option<T>): T? {
        if (option == TransactionDefinition.NAME) {
            return option.cast(value)
        }
        return null
    }
}
