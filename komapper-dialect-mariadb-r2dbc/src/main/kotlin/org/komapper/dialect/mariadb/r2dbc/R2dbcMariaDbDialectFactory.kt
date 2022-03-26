package org.komapper.dialect.mariadb.r2dbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory

class R2dbcMariaDbDialectFactory : R2dbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == MariaDbDialect.driver
    }

    override fun create(dataTypeProvider: R2dbcDataTypeProvider): R2dbcDialect {
        return R2dbcMariaDbDialect(dataTypeProvider)
    }
}
