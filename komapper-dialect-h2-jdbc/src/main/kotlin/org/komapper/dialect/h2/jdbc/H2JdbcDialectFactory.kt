package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class H2JdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == H2Dialect.driver
    }

    override fun create(): JdbcDialect {
        return H2JdbcDialect()
    }
}
