package org.komapper.dialect.h2.r2dbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcH2DataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == H2Dialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcH2DataTypeProvider(next)
    }
}
