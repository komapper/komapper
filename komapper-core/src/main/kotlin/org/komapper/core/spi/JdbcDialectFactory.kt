package org.komapper.core.spi

import org.komapper.core.JdbcDialect
import org.komapper.core.ThreadSafe
import org.komapper.core.jdbc.DataType

@ThreadSafe
interface JdbcDialectFactory {
    fun supports(subprotocol: String): Boolean
    fun create(dataTypes: List<DataType<*>>): JdbcDialect
}
