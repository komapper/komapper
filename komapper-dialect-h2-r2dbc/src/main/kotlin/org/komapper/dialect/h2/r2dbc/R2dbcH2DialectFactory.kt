package org.komapper.dialect.h2.r2dbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcH2DialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == H2Dialect.driver
    }

    override fun create(dataTypeProvider: R2dbcDataTypeProvider): R2dbcDialect {
        return R2dbcH2DialectImpl(dataTypeProvider)
    }
}
