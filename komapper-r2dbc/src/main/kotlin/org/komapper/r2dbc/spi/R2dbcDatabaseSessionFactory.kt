package org.komapper.r2dbc.spi

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.r2dbc.R2dbcDatabaseSession

@ThreadSafe
interface R2dbcDatabaseSessionFactory {
    fun create(connectionFactory: ConnectionFactory, logger: Logger): R2dbcDatabaseSession
}
