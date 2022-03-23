package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcSqlServerDataTypeProviderFactory : JdbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == SqlServerDialect.driver
    }

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcSqlServerDataTypeProvider(next)
    }
}
