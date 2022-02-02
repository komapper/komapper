package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcMariaDbDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == MariaDbDialect.DRIVER
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return JdbcMariaDbDialect(dataTypes)
    }
}