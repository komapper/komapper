package org.komapper.jdbc.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.jdbc.DefaultJdbcSession
import org.komapper.jdbc.JdbcSession
import java.util.ServiceLoader
import javax.sql.DataSource

@ThreadSafe
interface JdbcSessionFactory {
    fun create(dataSource: DataSource, logger: Logger): JdbcSession
}

object JdbcSessionProvider {
    fun get(dataSource: DataSource, logger: Logger): JdbcSession {
        val loader = ServiceLoader.load(JdbcSessionFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create(dataSource, logger) ?: DefaultJdbcSession(dataSource)
    }
}
