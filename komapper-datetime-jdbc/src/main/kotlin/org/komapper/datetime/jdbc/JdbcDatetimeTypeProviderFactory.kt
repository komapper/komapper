package org.komapper.datetime.jdbc

import org.komapper.core.spi.Prioritized.Companion.DEFAULT_PRIORITY
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcDatetimeTypeProviderFactory : JdbcDataTypeProviderFactory {
    override val priority: Int = DEFAULT_PRIORITY * 10

    override fun supports(driver: String): Boolean = true

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcDatetimeTypeProvider(next)
    }
}
