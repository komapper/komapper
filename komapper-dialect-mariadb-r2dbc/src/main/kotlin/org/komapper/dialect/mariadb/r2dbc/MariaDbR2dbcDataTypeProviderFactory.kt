package org.komapper.dialect.mariadb.r2dbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class MariaDbR2dbcDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == MariaDbDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return MariaDbR2dbcDataTypeProvider(next)
    }
}
