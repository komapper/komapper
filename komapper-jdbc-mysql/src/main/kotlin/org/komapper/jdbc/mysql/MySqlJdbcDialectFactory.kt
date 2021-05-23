package org.komapper.jdbc.mysql

import org.komapper.core.JdbcDialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.JdbcDialectFactory

class MySqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == MySqlJdbcDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return MySqlJdbcDialect(dataTypes)
    }
}
