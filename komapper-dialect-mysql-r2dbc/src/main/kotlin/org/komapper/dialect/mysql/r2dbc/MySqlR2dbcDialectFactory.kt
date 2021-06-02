package org.komapper.dialect.mysql.r2dbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class MySqlR2dbcDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == MySqlDialect.driver
    }

    override fun create(): R2dbcDialect {
        return MySqlR2dbcDialect()
    }
}
