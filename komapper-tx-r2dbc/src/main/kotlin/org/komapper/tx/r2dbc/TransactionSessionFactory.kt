package org.komapper.tx.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.spi.R2dbcSessionFactory

class TransactionSessionFactory : R2dbcSessionFactory {
    override fun create(connectionFactory: ConnectionFactory, logger: Logger): R2dbcSession {
        return TransactionSession(connectionFactory, logger)
    }
}
