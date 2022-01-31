package integration.jdbc

import integration.Direction
import integration.Json
import integration.JsonTest
import integration.SqlXmlTest
import integration.StringTest
import integration.UByteTest
import integration.UIntTest
import integration.UIntVersion
import integration.UShortTest
import integration.UUIDTest
import integration.UnsignedAddress
import integration.UnsignedAddress2
import integration.UnsignedIdentityStrategy
import integration.UnsignedSequenceStrategy
import integration.anyTest
import integration.arrayTest
import integration.bigDecimalTest
import integration.bigIntegerTest
import integration.booleanTest
import integration.byteArrayTest
import integration.byteTest
import integration.doubleTest
import integration.enumTest
import integration.floatTest
import integration.intTest
import integration.jsonTest
import integration.localDateTest
import integration.localDateTimeTest
import integration.localTimeTest
import integration.longTest
import integration.offsetDateTimeTest
import integration.setting.Dbms
import integration.setting.Run
import integration.shortTest
import integration.sqlXmlTest
import integration.stringTest
import integration.uByteTest
import integration.uIntTest
import integration.uShortTest
import integration.unsignedAddress
import integration.unsignedAddress2
import integration.unsignedIdentityStrategy
import integration.unsignedSequenceStrategy
import integration.uuidTest
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class JdbcDataTypeTest(val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any() {
        val m = Meta.anyTest
        val data = integration.AnyTest(
            1,
            integration.AnyPerson("ABC")
        )
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
        val data = integration.ArrayTest(1, array)
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
        val data = integration.ArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal() {
        val m = Meta.bigDecimalTest
        val data = integration.BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = Meta.bigIntegerTest
        val data = integration.BigIntegerTest(1, BigInteger.TEN)
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
        val bytes = data2.value.getBytes(1, 3)
        assertEquals(1, bytes[0])
        assertEquals(2, bytes[1])
        assertEquals(3, bytes[2])
    }

    @Test
    fun boolean() {
        val m = Meta.booleanTest
        val data = integration.BooleanTest(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = Meta.byteTest
        val data = integration.ByteTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
        val m = Meta.byteArrayTest
        val data = integration.ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.id, data2.id)
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
        val string = data2.value.getSubString(1, 3)
        assertEquals("abc", string)
    }

    @Test
    fun double() {
        val m = Meta.doubleTest
        val data = integration.DoubleTest(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum() {
        val m = Meta.enumTest
        val data = integration.EnumTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = Meta.floatTest
        val data = integration.FloatTest(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int() {
        val m = Meta.intTest
        val data = integration.IntTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = Meta.localDateTimeTest
        val data = integration.LocalDateTimeTest(
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
    fun localDate() {
        val m = Meta.localDateTest
        val data = integration.LocalDateTest(
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
    fun localTime() {
        val m = Meta.localTimeTest
        val data = integration.LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = Meta.longTest
        val data = integration.LongTest(1, 10L)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun offsetDateTime() {
        val m = Meta.offsetDateTimeTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = integration.OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.SQLSERVER])
    @Test
    fun offsetDateTime_offsetLost() {
        val m = Meta.offsetDateTimeTest
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = integration.OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        println(data2)
        assertNotNull(data2)
    }

    @Test
    fun short() {
        val m = Meta.shortTest
        val data = integration.ShortTest(1, 10)
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

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun json_postgresql() {
        val m = Meta.jsonTest
        val data = JsonTest(
            1,
            Json(
                """
            {"a": 100, "b": "Hello"}
                """.trimIndent()
            )
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)

        val result = db.runQuery {
            TemplateDsl
                .from("select value->'b' as x from json_test")
                .select { it.asT("x", Json::class)!! }
                .first()
        }
        assertEquals("\"Hello\"", result.data)
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
