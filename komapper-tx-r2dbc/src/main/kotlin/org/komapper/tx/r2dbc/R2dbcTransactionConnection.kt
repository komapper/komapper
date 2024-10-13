package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ValidationDepth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher

interface R2dbcTransactionConnection : Connection {
    suspend fun dispose()
    override fun close(): Publisher<Void>
}

private class R2dbcTransactionConnectionImpl(
    private val connection: Connection,
) : Connection by connection, R2dbcTransactionConnection {
    override suspend fun dispose() {
        val isValid = connection.validate(ValidationDepth.LOCAL).asFlow().single()
        if (isValid) {
            connection.close().asFlow().collect()
        }
    }

    override fun close(): Publisher<Void> {
        return emptyFlow<Void>().asPublisher()
    }
}

fun R2dbcTransactionConnection(connection: Connection): R2dbcTransactionConnection {
    return R2dbcTransactionConnectionImpl(connection)
}
