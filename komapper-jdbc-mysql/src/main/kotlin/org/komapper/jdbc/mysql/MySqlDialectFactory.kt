package org.komapper.jdbc.mysql

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory

class MySqlDialectFactory : DialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == "mysql"
    }

    override fun create(dataTypes: Set<DataType<*>>): Dialect {
        return MySqlDialect(dataTypes)
    }
}
