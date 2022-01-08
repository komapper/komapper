package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class H2JdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == H2Dialect.DRIVER
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return H2JdbcDialect(dataTypes)
    }
}
