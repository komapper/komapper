package org.komapper.dialect.postgresql.r2dbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory

class R2dbcPostgreSqlDataTypeProviderFactory : R2dbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == PostgreSqlDialect.driver
    }

    override fun create(next: R2dbcDataTypeProvider): R2dbcDataTypeProvider {
        return R2dbcPostgreSqlDataTypeProvider(next)
    }
}
