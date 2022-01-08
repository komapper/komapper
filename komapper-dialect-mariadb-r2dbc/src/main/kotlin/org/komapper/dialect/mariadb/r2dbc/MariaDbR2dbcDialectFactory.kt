package org.komapper.dialect.mariadb.r2dbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class MariaDbR2dbcDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == MariaDbDialect.DRIVER
    }

    override fun create(): R2dbcDialect {
        return MariaDbR2dbcDialect()
    }
}
