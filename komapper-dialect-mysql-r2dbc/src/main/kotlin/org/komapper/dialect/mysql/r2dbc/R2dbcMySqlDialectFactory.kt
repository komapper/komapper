package org.komapper.dialect.mysql.r2dbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcMySqlDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == MySqlDialect.DRIVER
    }

    override fun create(): R2dbcDialect {
        return R2dbcMySqlDialectImpl
    }
}
