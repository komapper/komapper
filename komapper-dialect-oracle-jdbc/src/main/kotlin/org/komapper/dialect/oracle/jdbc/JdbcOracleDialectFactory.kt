package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcOracleDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == OracleDialect.driver
    }

    override fun create(dataTypeProvider: JdbcDataTypeProvider): JdbcDialect {
        return JdbcOracleDialect(dataTypeProvider)
    }
}
