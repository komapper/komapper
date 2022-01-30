package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ValidationDepth
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher

interface TransactionConnection : Connection {
    suspend fun dispose()
    override fun close(): Publisher<Void>
}

internal class TransactionConnectionImpl(
    private val connection: Connection,
) : Connection by connection, TransactionConnection {

    override suspend fun dispose() {
        runCatching {
            val isValid = connection.validate(ValidationDepth.LOCAL).awaitSingle()
            if (isValid) {
                connection.close().awaitFirstOrNull()
            }
        }
    }

    override fun close(): Publisher<Void> {
        return emptyFlow<Void>().asPublisher()
    }
}
