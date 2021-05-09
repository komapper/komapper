package org.komapper.jdbc.h2

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory

class H2DialectFactory : DialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == H2Dialect.subprotocol
    }

    override fun create(dataTypes: List<DataType<*>>): Dialect {
        return H2Dialect(dataTypes)
    }
}
