package org.komapper.jdbc.spi

import org.komapper.core.spi.Prioritized
import org.komapper.jdbc.JdbcDataTypeProvider

interface JdbcDataTypeProviderFactory : Prioritized {
    fun supports(driver: String): Boolean
    fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider
}
