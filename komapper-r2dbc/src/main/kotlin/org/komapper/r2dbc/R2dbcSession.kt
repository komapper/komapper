package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher

/**
 * Represents a session for R2DBC access.
 */
@ThreadSafe
interface R2dbcSession {
    /**
     * The R2DBC connection.
     */
    val connection: Publisher<out Connection>
}

class DefaultR2DbcSession(private val connectionFactory: ConnectionFactory) : R2dbcSession {
    override val connection: Publisher<out Connection>
        get() = connectionFactory.create()
}
