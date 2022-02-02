package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcSqlServerDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == SqlServerDialect.DRIVER
    }

    override fun create(): R2dbcDialect {
        return R2dbcSqlServerDialect()
    }
}
