package org.komapper.jdbc.h2

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory

class H2DialectFactory : DialectFactory {
    override fun supports(subprotocol: String): Boolean {
        return subprotocol == "h2"
    }

    override fun create(dataTypes: Set<DataType<*>>): Dialect {
        return H2Dialect(dataTypes)
    }
}
