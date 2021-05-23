package org.komapper.jdbc.h2

import org.komapper.core.JdbcDialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.JdbcDialectFactory

class H2JdbcDialectFactory : JdbcDialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == H2JdbcDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return H2JdbcDialect(dataTypes)
    }
}
