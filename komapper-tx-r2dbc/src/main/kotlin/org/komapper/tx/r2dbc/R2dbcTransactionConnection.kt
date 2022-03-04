package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ValidationDepth
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.collect
import org.reactivestreams.Publisher

interface R2dbcTransactionConnection : Connection {
    suspend fun dispose()
    override fun close(): Publisher<Void>
}

internal class R2dbcTransactionConnectionImpl(
    private val connection: Connection,
) : Connection by connection, R2dbcTransactionConnection {

    override suspend fun dispose() {
        val isValid = connection.validate(ValidationDepth.LOCAL).asFlow().single()
        if (isValid) {
            connection.close().collect {}
        }
    }

    override fun close(): Publisher<Void> {
        return emptyFlow<Void>().asPublisher()
    }
}
