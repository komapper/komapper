package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class SqlServerJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == SqlServerDialect.DRIVER
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return SqlServerJdbcDialect(dataTypes)
    }
}
