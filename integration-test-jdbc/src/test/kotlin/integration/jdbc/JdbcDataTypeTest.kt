package integration.jdbc

import integration.meta
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
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
        val m = integration.AnyTest.meta
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
        val m = integration.ArrayTest.meta
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
        val m = integration.ArrayTest.meta
        val data = integration.ArrayTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal() {
        val m = integration.BigDecimalTest.meta
        val data = integration.BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = integration.BigIntegerTest.meta
        val data = integration.BigIntegerTest(1, BigInteger.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        val m = integration.BooleanTest.meta
        val data = integration.BooleanTest(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = integration.ByteTest.meta
        val data = integration.ByteTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
        val m = integration.ByteArrayTest.meta
        val data = integration.ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.id, data2.id)
        assertContentEquals(data.value, data2.value)
    }

    @Test
    fun double() {
        val m = integration.DoubleTest.meta
        val data = integration.DoubleTest(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = integration.FloatTest.meta
        val data = integration.FloatTest(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int() {
        val m = integration.IntTest.meta
        val data = integration.IntTest(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = integration.LocalDateTimeTest.meta
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
        val m = integration.LocalDateTest.meta
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
        val m = integration.LocalTimeTest.meta
        val data = integration.LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = integration.LongTest.meta
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
        val m = integration.OffsetDateTimeTest.meta
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

    @Run(unless = [Dbms.H2])
    @Test
    fun offsetDateTime_unlessH2() {
        val m = integration.OffsetDateTimeTest.meta
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = integration.OffsetDateTimeTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun short() {
        val m = integration.ShortTest.meta
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
        val m = integration.SqlXmlTest.meta
        val value = db.dataFactory.createSQLXML()
        val xml = "<test>hello</test>"
        value.string = xml
        val data = integration.SqlXmlTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(xml, data2.value!!.string)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun sqlXml_null() {
        val m = integration.SqlXmlTest.meta
        val data = integration.SqlXmlTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun string() {
        val m = integration.StringTest.meta
        val data = integration.StringTest(1, "ABC")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun json_postgresql() {
        val m = integration.JsonTest.meta
        val data = integration.JsonTest(
            1,
            integration.Json(
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
                .select { it.asT("x", integration.Json::class)!! }
                .first()
        }
        assertEquals("\"Hello\"", result.data)
    }

    @Test
    fun unsignedByte() {
        val m = integration.UByteTest.meta
        val data = integration.UByteTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_maxValue() {
        val m = integration.UByteTest.meta
        val data = integration.UByteTest(1, UByte.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null() {
        val m = integration.UByteTest.meta
        val data = integration.UByteTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt() {
        val m = integration.UIntTest.meta
        val data = integration.UIntTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_maxValue() {
        val m = integration.UIntTest.meta
        val data = integration.UIntTest(1, UInt.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null() {
        val m = integration.UIntTest.meta
        val data = integration.UIntTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort() {
        val m = integration.UShortTest.meta
        val data = integration.UShortTest(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsigned_autoIncrement() {
        val m = integration.UnsignedIdentityStrategy.meta
        val data = integration.UnsignedIdentityStrategy(null, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun unsigned_sequence() {
        val m = integration.UnsignedSequenceStrategy.meta
        val data = integration.UnsignedSequenceStrategy(0u, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version() {
        val m = integration.UnsignedAddress.meta
        val data = integration.UnsignedAddress(16u, "STREET 16", 0u)
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
        val m = integration.UnsignedAddress2.meta
        val data = integration.UnsignedAddress2(16u, "STREET 16", null)
        val data2 = db.runQuery { QueryDsl.insert(m).single(data) }
        db.runQuery {
            QueryDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.addressId eq 16u }.first()
        }
        assertEquals(16u, data3.addressId)
        assertEquals(integration.UIntVersion(1u), data3.version)
    }

    @Test
    fun unsignedShort_maxValue() {
        val m = integration.UShortTest.meta
        val data = integration.UShortTest(1, UShort.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null() {
        val m = integration.UShortTest.meta
        val data = integration.UShortTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid() {
        val m = integration.UUIDTest.meta
        val value = UUID.randomUUID()
        val data = integration.UUIDTest(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null() {
        val m = integration.UUIDTest.meta
        val data = integration.UUIDTest(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
