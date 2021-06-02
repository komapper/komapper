package org.komapper.jdbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.jdbc.DataType
import org.komapper.jdbc.JdbcDialect

@ThreadSafe
interface JdbcDialectFactory {
    fun supports(driver: String): Boolean
    fun create(dataTypes: List<DataType<*>>): JdbcDialect
}
