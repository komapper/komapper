package integration.r2dbc.postgresql

import integration.core.enumclass.Mood
import io.netty.buffer.ByteBufAllocator
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.codec.CodecRegistry
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.postgresql.extension.CodecRegistrar
import org.reactivestreams.Publisher

class PosgreSqlCodecRegistrar : CodecRegistrar {
    override fun register(
        connection: PostgresqlConnection,
        allocator: ByteBufAllocator,
        registry: CodecRegistry,
    ): Publisher<Void> {
        val registrar = EnumCodec.builder().withEnum("mood", Mood::class.java).build()
        return registrar.register(connection, allocator, registry)
    }
}
