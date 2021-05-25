package org.komapper.tx.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.r2dbc.R2dbcDatabaseSession
import org.komapper.r2dbc.spi.R2dbcDatabaseSessionFactory

class TransactionDatabaseSessionFactory : R2dbcDatabaseSessionFactory {
    override fun create(connectionFactory: ConnectionFactory, logger: Logger): R2dbcDatabaseSession {
        return TransactionDatabaseSession(connectionFactory, logger)
    }
}
