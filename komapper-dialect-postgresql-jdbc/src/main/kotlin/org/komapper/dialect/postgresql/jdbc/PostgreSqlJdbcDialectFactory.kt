package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class PostgreSqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == PostgreSqlDialect.DRIVER
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return PostgreSqlJdbcDialect(dataTypes)
    }
}
