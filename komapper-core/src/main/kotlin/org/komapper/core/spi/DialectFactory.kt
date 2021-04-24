package org.komapper.core.spi

import org.komapper.core.Dialect
import org.komapper.core.jdbc.DataType

interface DialectFactory {
    fun supports(subprotocol: String): Boolean
    fun create(dataTypes: Set<DataType<*>>): Dialect
}
