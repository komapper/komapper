package org.komapper.datetime.r2dbc

import org.komapper.core.spi.Prioritized.Companion.DEFAULT_PRIORITY
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcDatetimeTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override val priority: Int = DEFAULT_PRIORITY * 10

    override fun supports(driver: String): Boolean = true

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcDatetimeTypeProvider(next)
    }
}
