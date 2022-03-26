package org.komapper.dialect.postgresql.r2dbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcPostgreSqlDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == PostgreSqlDialect.driver
    }

    override fun create(): R2dbcDialect {
        return R2dbcPostgreSqlDialectImpl
    }
}
