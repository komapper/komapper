package org.komapper.tx.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.spi.R2dbcSessionFactory

class R2dbcTransactionSessionFactory : R2dbcSessionFactory {
    override fun create(connectionFactory: ConnectionFactory, loggerFacade: LoggerFacade): R2dbcSession {
        return R2dbcTransactionSession(connectionFactory, loggerFacade)
    }
}
