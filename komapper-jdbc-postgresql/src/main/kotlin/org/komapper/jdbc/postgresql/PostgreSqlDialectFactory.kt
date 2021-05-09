package org.komapper.jdbc.postgresql

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory

class PostgreSqlDialectFactory : DialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == PostgreSqlDialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): Dialect {
        return PostgreSqlDialect(dataTypes)
    }
}
