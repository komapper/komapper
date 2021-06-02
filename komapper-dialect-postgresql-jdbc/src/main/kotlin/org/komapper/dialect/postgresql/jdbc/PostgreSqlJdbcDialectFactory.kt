package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.DataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class PostgreSqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == PostgreSqlDialect.driver
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return PostgreSqlJdbcDialect(dataTypes)
    }
}
