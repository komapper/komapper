package org.komapper.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.core.spi.findByPriority
import org.komapper.jdbc.spi.JdbcSessionFactory
import java.util.ServiceLoader
import javax.sql.DataSource

/**
 * The provider of [JdbcSession].
 */
object JdbcSessions {
    /**
     * @param dataSource the data source
     * @param loggerFacade the logger facade
     * @return the [JdbcSession] instance.
     */
    fun get(dataSource: DataSource, loggerFacade: LoggerFacade): JdbcSession {
        val loader = ServiceLoader.load(JdbcSessionFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(dataSource, loggerFacade) ?: DefaultJdbcSession(dataSource)
    }
}
