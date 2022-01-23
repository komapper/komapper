package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class OracleJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == OracleDialect.DRIVER
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return OracleJdbcDialect(dataTypes)
    }
}
