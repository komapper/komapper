package org.komapper.dialect.mysql.r2dbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcMySqlDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == MySqlDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcMySqlDataTypeProvider(next)
    }
}
