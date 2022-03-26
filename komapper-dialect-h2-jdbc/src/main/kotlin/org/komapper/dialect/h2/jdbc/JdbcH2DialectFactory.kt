package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcH2DialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == H2Dialect.driver
    }

    override fun create(dataTypeProvider: JdbcDataTypeProvider): JdbcDialect {
        return JdbcH2DialectImpl(dataTypeProvider)
    }
}
