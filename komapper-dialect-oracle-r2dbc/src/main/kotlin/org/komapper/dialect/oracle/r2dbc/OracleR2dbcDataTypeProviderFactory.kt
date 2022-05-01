package org.komapper.dialect.oracle.r2dbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class OracleR2dbcDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == OracleDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return OracleR2dbcDataTypeProvider(next)
    }
}
