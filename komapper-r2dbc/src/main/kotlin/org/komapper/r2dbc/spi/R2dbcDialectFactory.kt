package org.komapper.r2dbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.core.spi.Prioritized
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect

@ThreadSafe
interface R2dbcDialectFactory : Prioritized {
    fun supports(driver: String): Boolean
    fun create(dataTypeProvider: R2dbcDataTypeProvider): R2dbcDialect
}
