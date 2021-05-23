package org.komapper.jdbc.postgresql

import org.komapper.core.JdbcDialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.JdbcDialectFactory

class PostgreSqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == PostgreSqlJdbcDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return PostgreSqlJdbcDialect(dataTypes)
    }
}
