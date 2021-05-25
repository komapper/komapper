package org.komapper.dialect.h2.r2dbx

import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class H2R2dbcDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == H2R2dbcDialect.driver
    }

    override fun create(): R2dbcDialect {
        return H2R2dbcDialect()
    }
}
