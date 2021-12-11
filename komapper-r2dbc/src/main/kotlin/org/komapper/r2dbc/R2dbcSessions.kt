package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.LoggerFacade
import org.komapper.core.spi.findByPriority
import org.komapper.r2dbc.spi.R2dbcSessionFactory
import java.util.ServiceLoader

object R2dbcSessions {
    fun get(connectionFactory: ConnectionFactory, loggerFacade: LoggerFacade): R2dbcSession {
        val loader = ServiceLoader.load(R2dbcSessionFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(connectionFactory, loggerFacade) ?: DefaultR2DbcSession(connectionFactory)
    }
}
