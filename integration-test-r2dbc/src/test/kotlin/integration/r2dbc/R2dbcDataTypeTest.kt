package integration.r2dbc

import integration.core.Dbms
import integration.core.Direction
import integration.core.EnumTest
import integration.core.InstantTest
import integration.core.Run
import integration.core.UUIDTest
import integration.core.enumTest
import integration.core.instantTest
import integration.core.uuidTest
import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcDataTypeTest(val db: R2dbcDatabase) {

    @Test
    fun enum(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumTest
        val data = EnumTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun blob(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.blobTest
        val p = flowOf(ByteBuffer.wrap(byteArrayOf(1, 2, 3))).asPublisher()
        val data = BlobTest(1, Blob.from(p))
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val buffer = data2.value.stream().awaitFirst()
        assertEquals(1, buffer[0])
        assertEquals(2, buffer[1])
        assertEquals(3, buffer[2])
    }

    @Test
    fun clob(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobTest
        val p = flowOf(CharBuffer.wrap("abc")).asPublisher()
        val data = ClobTest(1, Clob.from(p))
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val buffer = data2.value.stream().awaitFirst()
        assertEquals('a', buffer[0])
        assertEquals('b', buffer[1])
        assertEquals('c', buffer[2])
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uuidTest
        val value = UUID.randomUUID()
        val data = UUIDTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun instant(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.instantTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val value = dateTime.toInstant(ZoneOffset.UTC)
        val data = InstantTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uuidTest
        val data = UUIDTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
