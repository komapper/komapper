package org.komapper.dialect.oracle.r2dbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class OracleR2dbcDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == OracleDialect.driver
    }

    override fun create(): R2dbcDialect {
        return OracleR2dbcDialect()
    }
}
