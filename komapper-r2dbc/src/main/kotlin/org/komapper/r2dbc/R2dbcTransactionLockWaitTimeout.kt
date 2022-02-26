package org.komapper.r2dbc

import io.r2dbc.spi.Option
import io.r2dbc.spi.TransactionDefinition
import java.time.Duration

data class R2dbcTransactionLockWaitTimeout(val value: Duration) : TransactionDefinition {
    override fun <T : Any> getAttribute(option: Option<T>): T? {
        if (option == TransactionDefinition.LOCK_WAIT_TIMEOUT) {
            return option.cast(value)
        }
        return null
    }
}
