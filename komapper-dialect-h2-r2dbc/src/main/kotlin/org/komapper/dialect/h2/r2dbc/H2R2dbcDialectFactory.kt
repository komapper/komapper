package org.komapper.dialect.h2.r2dbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class H2R2dbcDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == H2Dialect.driver
    }

    override fun create(): R2dbcDialect {
        return H2R2dbcDialect()
    }
}
