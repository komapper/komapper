package org.komapper.dialect.mariadb.r2dbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcMariaDbDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == MariaDbDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcMariaDbDataTypeProvider(next)
    }
}
