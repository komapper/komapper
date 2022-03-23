package org.komapper.jdbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.core.spi.Prioritized
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect

@ThreadSafe
interface JdbcDialectFactory : Prioritized {
    fun supports(driver: String): Boolean
    fun create(dataTypeProvider: JdbcDataTypeProvider): JdbcDialect
}
