package integration.r2dbc

import integration.core.BigDecimalTest
import integration.core.BigIntegerTest
import integration.core.BooleanTest
import integration.core.ByteArrayTest
import integration.core.ByteTest
import integration.core.Dbms
import integration.core.Direction
import integration.core.DoubleTest
import integration.core.EnumOrdinalTest
import integration.core.EnumTest
import integration.core.FloatTest
import integration.core.InstantTest
import integration.core.IntTest
import integration.core.LocalDateTest
import integration.core.LocalDateTimeTest
import integration.core.LocalTimeTest
import integration.core.LongTest
import integration.core.OffsetDateTimeTest
import integration.core.Run
import integration.core.ShortTest
import integration.core.StringTest
import integration.core.UByteTest
import integration.core.UIntTest
import integration.core.UIntVersion
import integration.core.UShortTest
import integration.core.UUIDTest
import integration.core.UnsignedAddress
import integration.core.UnsignedAddress2
import integration.core.UnsignedIdentityStrategy
import integration.core.UnsignedSequenceStrategy
import integration.core.bigDecimalTest
import integration.core.bigIntegerTest
import integration.core.booleanTest
import integration.core.byteArrayTest
import integration.core.byteTest
import integration.core.doubleTest
import integration.core.enumOrdinalTest
import integration.core.enumTest
import integration.core.floatTest
import integration.core.instantTest
import integration.core.intTest
import integration.core.localDateTest
import integration.core.localDateTimeTest
import integration.core.localTimeTest
import integration.core.longTest
import integration.core.offsetDateTimeTest
import integration.core.shortTest
import integration.core.stringTest
import integration.core.uByteTest
import integration.core.uIntTest
import integration.core.uShortTest
import integration.core.unsignedAddress
import integration.core.unsignedAddress2
import integration.core.unsignedIdentityStrategy
import integration.core.unsignedSequenceStrategy
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
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(R2dbcEnv::class)
class R2dbcDataTypeTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun array(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayTest
        val array = arrayOf("A", "B", "C")
        val data = ArrayTest(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val value = data2.value!!
        assertEquals(3, value.size)
        assertEquals("A", value[0])
        assertEquals("B", value[1])
        assertEquals("C", value[2])
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun array_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayTest
        val data = ArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun arrayOfNullable(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayOfNullableTest
        val array = arrayOf("A", null, "C")
        val data = ArrayOfNullableTest(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val value = data2.value!!
        assertEquals(3, value.size)
        assertEquals("A", value[0])
        assertNull(value[1])
        assertEquals("C", value[2])
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun arrayOfNullable_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayOfNullableTest
        val data = ArrayOfNullableTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigDecimalTest
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigDecimal_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigDecimalTest
        val data = BigDecimalTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigIntegerTest
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigIntegerTest
        val data = BigIntegerTest(1, null)
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
        val buffer = data2.value!!.stream().awaitFirst()
        assertEquals(1, buffer[0])
        assertEquals(2, buffer[1])
        assertEquals(3, buffer[2])
    }

    @Test
    fun blob_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.blobTest
        val data = BlobTest(1, null)
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.booleanTest
        val data = BooleanTest(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.booleanTest
        val data = BooleanTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteTest
        val data = ByteTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteTest
        val data = ByteTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteArrayTest
        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.id, data2.id)
        assertContentEquals(data.value, data2.value)
    }

    @Test
    fun byteArray_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteArrayTest
        val data = ByteArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
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
        val buffer = data2.value!!.stream().awaitFirst()
        assertEquals('a', buffer[0])
        assertEquals('b', buffer[1])
        assertEquals('c', buffer[2])
    }

    @Test
    fun clob_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobTest
        val data = ClobTest(1, null)
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.doubleTest
        val data = DoubleTest(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.doubleTest
        val data = DoubleTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

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
    fun enum_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumTest
        val data = EnumTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_ordinal(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumOrdinalTest
        val data = EnumOrdinalTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_ordinal_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumOrdinalTest
        val data = EnumOrdinalTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
    @Test
    fun float(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.floatTest
        val data = FloatTest(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.floatTest
        val data = FloatTest(1, null)
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

    @Test
    fun instant_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.instantTest
        val data = InstantTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intTest
        val data = IntTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intTest
        val data = IntTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTimeTest
        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTimeTest
        val data = LocalDateTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTest
        val data = LocalDateTest(
            1,
            LocalDate.of(2019, 6, 1)
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTest
        val data = LocalDateTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localTimeTest
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localTimeTest
        val data = LocalTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.longTest
        val data = LongTest(1, 10L)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.longTest
        val data = LongTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun offsetDateTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun offsetDateTime_postgresql(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val expected = OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
        assertEquals(expected, data2.value)
    }

    @Test
    fun offsetDateTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeTest
        val data = OffsetDateTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.shortTest
        val data = ShortTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.shortTest
        val data = ShortTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun string(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.stringTest
        val data = StringTest(1, "ABC")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun string_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.stringTest
        val data = StringTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteTest
        val data = UByteTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteTest
        val data = UByteTest(1, UByte.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteTest
        val data = UByteTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntTest
        val data = UIntTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntTest
        val data = UIntTest(1, UInt.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntTest
        val data = UIntTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortTest
        val data = UShortTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsigned_autoIncrement(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedIdentityStrategy
        val data = UnsignedIdentityStrategy(null, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun unsigned_sequence(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedSequenceStrategy
        val data = UnsignedSequenceStrategy(0u, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedAddress
        val data = UnsignedAddress(16u, "STREET 16", 0u)
        val data2 = db.runQuery { QueryDsl.insert(m).single(data) }
        db.runQuery {
            QueryDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.addressId eq 16u }.first()
        }
        assertEquals(16u, data3.addressId)
        assertEquals(1u, data3.version)
    }

    @Test
    fun unsigned_version_valueClass(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedAddress2
        val data = UnsignedAddress2(16u, "STREET 16", null)
        val data2 = db.runQuery { QueryDsl.insert(m).single(data) }
        db.runQuery {
            QueryDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.addressId eq 16u }.first()
        }
        assertEquals(16u, data3.addressId)
        assertEquals(UIntVersion(1u), data3.version)
    }

    @Test
    fun unsignedShort_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortTest
        val data = UShortTest(1, UShort.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortTest
        val data = UShortTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
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
