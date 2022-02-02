package org.komapper.dialect.h2.r2dbc

import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcH2DialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == R2dbcH2Dialect.DRIVER
    }

    override fun create(): R2dbcDialect {
        return R2dbcH2Dialect()
    }
}
