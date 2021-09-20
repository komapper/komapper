package org.komapper.r2dbc.spi

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.r2dbc.DefaultR2DbcSession
import org.komapper.r2dbc.R2dbcSession
import java.util.ServiceLoader

@ThreadSafe
interface R2dbcSessionFactory {
    fun create(connectionFactory: ConnectionFactory, logger: Logger): R2dbcSession
}

object R2dbcSessionProvider {
    fun get(connectionFactory: ConnectionFactory, logger: Logger): R2dbcSession {
        val loader = ServiceLoader.load(R2dbcSessionFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create(connectionFactory, logger) ?: DefaultR2DbcSession(connectionFactory)
    }
}
