package integration.r2dbc

import integration.Direction
import integration.UUIDTest
import integration.enumTest
import integration.setting.Dbms
import integration.setting.Run
import integration.uuidTest
import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class R2dbcDataTypeTest(val db: R2dbcDatabase) {

    @Test
    fun enum() = inTransaction(db) {
        val m = Meta.enumTest
        val data = integration.EnumTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun blob() = inTransaction(db) {
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
    fun clob() = inTransaction(db) {
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
    fun uuid() = inTransaction(db) {
        val m = Meta.uuidTest
        val value = UUID.randomUUID()
        val data = UUIDTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null() = inTransaction(db) {
        val m = Meta.uuidTest
        val data = UUIDTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
