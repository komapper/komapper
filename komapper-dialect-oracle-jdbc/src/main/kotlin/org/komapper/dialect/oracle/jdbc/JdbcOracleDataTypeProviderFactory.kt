package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcOracleDataTypeProviderFactory : JdbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == OracleDialect.driver
    }

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcOracleDataTypeProvider(next)
    }
}
