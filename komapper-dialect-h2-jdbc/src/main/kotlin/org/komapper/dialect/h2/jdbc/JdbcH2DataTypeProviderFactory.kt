package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcH2DataTypeProviderFactory : JdbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == H2Dialect.driver
    }

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcH2DataTypeProvider(next)
    }
}
