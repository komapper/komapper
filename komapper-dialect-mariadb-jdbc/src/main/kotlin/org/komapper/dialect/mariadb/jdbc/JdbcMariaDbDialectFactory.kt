package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcMariaDbDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == MariaDbDialect.driver
    }

    override fun create(dataTypeProvider: JdbcDataTypeProvider): JdbcDialect {
        return JdbcMariaDbDialect(dataTypeProvider)
    }
}
