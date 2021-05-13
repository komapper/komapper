package org.komapper.core.spi

import org.komapper.core.Dialect
import org.komapper.core.ThreadSafe
import org.komapper.core.jdbc.DataType

@ThreadSafe
interface DialectFactory {
    fun supports(subprotocol: String): Boolean
    fun create(dataTypes: List<DataType<*>>): Dialect
}
