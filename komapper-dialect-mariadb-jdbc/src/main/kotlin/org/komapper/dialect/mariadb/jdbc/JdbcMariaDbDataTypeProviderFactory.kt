package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcMariaDbDataTypeProviderFactory : JdbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == MariaDbDialect.driver
    }

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcMariaDbDataTypeProvider(next)
    }
}
