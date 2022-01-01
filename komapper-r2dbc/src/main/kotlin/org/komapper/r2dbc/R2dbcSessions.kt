package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.LoggerFacade
import org.komapper.core.spi.findByPriority
import org.komapper.r2dbc.spi.R2dbcSessionFactory
import java.util.ServiceLoader

/**
 * The provider of [R2dbcSession].
 */
object R2dbcSessions {
    /**
     * @param connectionFactory the connection factory
     * @param loggerFacade the logger facade
     * @return the [R2dbcSession] instance.
     */
    fun get(connectionFactory: ConnectionFactory, loggerFacade: LoggerFacade): R2dbcSession {
        val loader = ServiceLoader.load(R2dbcSessionFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(connectionFactory, loggerFacade) ?: DefaultR2DbcSession(connectionFactory)
    }
}
