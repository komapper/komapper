package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcSqlServerDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == SqlServerDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcSqlServerDataTypeProvider(next)
    }
}
