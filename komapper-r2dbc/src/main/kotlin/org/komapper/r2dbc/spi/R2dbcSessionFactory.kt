package org.komapper.r2dbc.spi

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.Prioritized
import org.komapper.r2dbc.R2dbcSession

@ThreadSafe
interface R2dbcSessionFactory : Prioritized {
    fun create(connectionFactory: ConnectionFactory, logger: Logger): R2dbcSession
}
