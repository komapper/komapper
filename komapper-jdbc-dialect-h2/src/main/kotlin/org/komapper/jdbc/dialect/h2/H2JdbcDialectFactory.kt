package org.komapper.jdbc.dialect.h2

import org.komapper.jdbc.DataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class H2JdbcDialectFactory : JdbcDialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == H2JdbcDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return H2JdbcDialect(dataTypes)
    }
}
