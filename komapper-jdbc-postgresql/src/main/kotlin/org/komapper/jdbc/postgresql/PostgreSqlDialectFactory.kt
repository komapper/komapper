package org.komapper.jdbc.postgresql

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory

class PostgreSqlDialectFactory : DialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == "postgresql"
    }

    override fun create(dataTypes: Set<DataType<*>>): Dialect {
        return PostgreSqlDialect(dataTypes)
    }
}
