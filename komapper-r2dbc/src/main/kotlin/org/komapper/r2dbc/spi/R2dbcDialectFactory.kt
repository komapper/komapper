package org.komapper.r2dbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.r2dbc.R2dbcDialect

@ThreadSafe
interface R2dbcDialectFactory {
    fun supports(driver: String): Boolean
    fun create(): R2dbcDialect
}
