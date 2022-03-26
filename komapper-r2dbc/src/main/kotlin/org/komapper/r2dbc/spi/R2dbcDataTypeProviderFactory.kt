package org.komapper.r2dbc.spi

import org.komapper.core.spi.Prioritized
import org.komapper.r2dbc.R2dbcDataTypeProvider

interface R2dbcDataTypeProviderFactory : Prioritized {
    fun supports(driver: String): Boolean
    fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider
}
