package org.komapper.jdbc.mysql

import org.komapper.jdbc.DataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class MySqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == MySqlJdbcDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return MySqlJdbcDialect(dataTypes)
    }
}
