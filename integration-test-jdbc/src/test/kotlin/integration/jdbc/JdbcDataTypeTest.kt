package integration.jdbc

import integration.core.AnyPerson
import integration.core.AnyTest
import integration.core.BigDecimalTest
import integration.core.BigIntegerTest
import integration.core.BooleanTest
import integration.core.ByteArrayTest
import integration.core.ByteTest
import integration.core.Dbms
import integration.core.Direction
import integration.core.DoubleTest
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
import integration.core.anyTest
import integration.core.bigDecimalTest
import integration.core.bigIntegerTest
import integration.core.booleanTest
import integration.core.byteArrayTest
import integration.core.byteTest
import integration.core.doubleTest
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
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.math.BigInteger
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcDataTypeTest(val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any() {
        val m = Meta.anyTest
        val data = AnyTest(
            1,
            AnyPerson("ABC")
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any_null() {
        val m = Meta.anyTest
        val data = AnyTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun array() {
        val m = Meta.arrayTest
        val array = db.dataFactory.createArrayOf("text", listOf("A", "B", "C"))
        val data = ArrayTest(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val anyArray = data2.value!!.getArray(1, 3) as Array<*>
        assertEquals(3, anyArray.size)
        assertEquals("A", anyArray[0])
        assertEquals("B", anyArray[1])
        assertEquals("C", anyArray[2])
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun array_null() {
        val m = Meta.arrayTest
        val data = ArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal() {
        val m = Meta.bigDecimalTest
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigDecimal_null() {
        val m = Meta.bigDecimalTest
        val data = BigDecimalTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = Meta.bigIntegerTest
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger_null() {
        val m = Meta.bigIntegerTest
        val data = BigIntegerTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun blob() {
        val m = Meta.blobTest
        val blob = db.dataFactory.createBlob()
        blob.setBytes(1, byteArrayOf(1, 2, 3))
        val data = BlobTest(1, blob)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val bytes = data2.value!!.getBytes(1, 3)
        assertEquals(1, bytes[0])
        assertEquals(2, bytes[1])
        assertEquals(3, bytes[2])
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun blob_null() {
        val m = Meta.blobTest
        val data = BlobTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        val m = Meta.booleanTest
        val data = BooleanTest(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean_null() {
        val m = Meta.booleanTest
        val data = BooleanTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = Meta.byteTest
        val data = ByteTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte_null() {
        val m = Meta.byteTest
        val data = ByteTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
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
    fun byteArray_null() {
        val m = Meta.byteArrayTest
        val data = ByteArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertContentEquals(data.value, data2.value)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun clob() {
        val m = Meta.clobTest
        val clob = db.dataFactory.createClob()
        clob.setString(1, "abc")
        val data = ClobTest(1, clob)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val string = data2.value!!.getSubString(1, 3)
        assertEquals("abc", string)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun clob_null() {
        val m = Meta.clobTest
        val data = ClobTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double() {
        val m = Meta.doubleTest
        val data = DoubleTest(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double_null() {
        val m = Meta.doubleTest
        val data = DoubleTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum() {
        val m = Meta.enumTest
        val data = EnumTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_null() {
        val m = Meta.enumTest
        val data = EnumTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = Meta.floatTest
        val data = FloatTest(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float_null() {
        val m = Meta.floatTest
        val data = FloatTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun instant() {
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

    @Run(unless = [Dbms.MARIADB])
    @Test
    fun instant_null() {
        val m = Meta.instantTest
        val data = InstantTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.MARIADB])
    @Test
    fun instant_automatic() {
        val m = Meta.instantTest
        val data = InstantTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNotNull(data2.value)
    }

    @Test
    fun int() {
        val m = Meta.intTest
        val data = IntTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int_null() {
        val m = Meta.intTest
        val data = IntTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
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
    fun localDateTime_null() {
        val m = Meta.localDateTimeTest
        val data = LocalDateTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate() {
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
    fun localDate_null() {
        val m = Meta.localDateTest
        val data = LocalDateTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime() {
        val m = Meta.localTimeTest
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime_null() {
        val m = Meta.localTimeTest
        val data = LocalTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = Meta.longTest
        val data = LongTest(1, 10L)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long_null() {
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
    fun offsetDateTime() {
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

    @Run(onlyIf = [Dbms.MYSQL])
    @Test
    fun offsetDateTime_mysql() {
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

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun offsetDateTime_postgresql() {
        val m = Meta.offsetDateTimeTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        println(data2)
        assertNotEquals(data, data2)
        assertEquals(value.toInstant(), data2.value!!.toInstant())
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MYSQL, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun offsetDateTime_null() {
        val m = Meta.offsetDateTimeTest
        val data = OffsetDateTimeTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short() {
        val m = Meta.shortTest
        val data = ShortTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short_null() {
        val m = Meta.shortTest
        val data = ShortTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun sqlXml() {
        val m = Meta.sqlXmlTest
        val value = db.dataFactory.createSQLXML()
        val xml = "<test>hello</test>"
        value.string = xml
        val data = SqlXmlTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(xml, data2.value!!.string)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun sqlXml_null() {
        val m = Meta.sqlXmlTest
        val data = SqlXmlTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun string() {
        val m = Meta.stringTest
        val data = StringTest(1, "ABC")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun string_null() {
        val m = Meta.stringTest
        val data = StringTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte() {
        val m = Meta.uByteTest
        val data = UByteTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_maxValue() {
        val m = Meta.uByteTest
        val data = UByteTest(1, UByte.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null() {
        val m = Meta.uByteTest
        val data = UByteTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt() {
        val m = Meta.uIntTest
        val data = UIntTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_maxValue() {
        val m = Meta.uIntTest
        val data = UIntTest(1, UInt.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null() {
        val m = Meta.uIntTest
        val data = UIntTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort() {
        val m = Meta.uShortTest
        val data = UShortTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsigned_autoIncrement() {
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
    fun unsigned_sequence() {
        val m = Meta.unsignedSequenceStrategy
        val data = UnsignedSequenceStrategy(0u, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version() {
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
    fun unsigned_version_valueClass() {
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
    fun unsignedShort_maxValue() {
        val m = Meta.uShortTest
        val data = UShortTest(1, UShort.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null() {
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
    fun uuid() {
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
    fun uuid_null() {
        val m = Meta.uuidTest
        val data = UUIDTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
