package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class SqlServerJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == SqlServerDialect.driver
    }

    override fun create(): JdbcDialect {
        return SqlServerJdbcDialect()
    }
}
