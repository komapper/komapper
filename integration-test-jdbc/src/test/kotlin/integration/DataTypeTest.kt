package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.EntityDsl
import org.komapper.jdbc.dsl.TemplateDsl
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@ExtendWith(Env::class)
class DataTypeTest(val db: Database) {

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any() {
        val m = AnyTest.meta
        val data = AnyTest(
            1,
            AnyPerson("ABC")
        )
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigDecimal() {
        val m = BigDecimalTest.meta
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = BigIntegerTest.meta
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        val m = BooleanTest.meta
        val data = BooleanTest(1, true)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = ByteTest.meta
        val data = ByteTest(1, 10)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
        val m = ByteArrayTest.meta
        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data.id, data2.id)
        Assertions.assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double() {
        val m = DoubleTest.meta
        val data = DoubleTest(1, 10.0)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = FloatTest.meta
        val data = FloatTest(1, 10.0f)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun int() {
        val m = IntTest.meta
        val data = IntTest(1, 10)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = LocalDateTimeTest.meta
        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate() {
        val m = LocalDateTest.meta
        val data = LocalDateTest(
            1,
            LocalDate.of(2019, 6, 1)
        )
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime() {
        val m = LocalTimeTest.meta
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = LongTest.meta
        val data = LongTest(1, 10L)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun offsetDateTime() {
        val m = OffsetDateTimeTest.meta
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.H2])
    @Test
    fun offsetDateTime_unlessH2() {
        val m = OffsetDateTimeTest.meta
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertNotNull(data2)
    }

    @Test
    fun short() {
        val m = ShortTest.meta
        val data = ShortTest(1, 10)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun string() {
        val m = StringTest.meta
        val data = StringTest(1, "ABC")
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun json_postgresql() {
        val m = JsonTest.meta
        val data = JsonTest(
            1,
            Json(
                """
            {"a": 100, "b": "Hello"}
                """.trimIndent()
            )
        )
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
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
        val m = UByteTest.meta
        val data = UByteTest(1, 10u)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_maxValue() {
        val m = UByteTest.meta
        val data = UByteTest(1, UByte.MAX_VALUE)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null() {
        val m = UByteTest.meta
        val data = UByteTest(1, null)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt() {
        val m = UIntTest.meta
        val data = UIntTest(1, 10u)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_maxValue() {
        val m = UIntTest.meta
        val data = UIntTest(1, UInt.MAX_VALUE)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null() {
        val m = UIntTest.meta
        val data = UIntTest(1, null)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort() {
        val m = UShortTest.meta
        val data = UShortTest(1, 10u)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsigned_autoIncrement() {
        val m = UnsignedIdentityStrategy.meta
        val data = UnsignedIdentityStrategy(null, "test")
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1u }
        }
        assertNotNull(data2)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun unsigned_sequence() {
        val m = UnsignedSequenceStrategy.meta
        val data = UnsignedSequenceStrategy(0u, "test")
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1u }
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version() {
        val m = UnsignedAddress.meta
        val data = UnsignedAddress(16u, "STREET 16", 0u)
        val data2 = db.runQuery { EntityDsl.insert(m).single(data) }
        db.runQuery {
            EntityDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            EntityDsl.from(m).first { m.addressId eq 16u }
        }
        assertEquals(16u, data3.addressId)
        assertEquals(1u, data3.version)
    }

    @Test
    fun unsigned_version_valueClass() {
        val m = UnsignedAddress2.meta
        val data = UnsignedAddress2(16u, "STREET 16", null)
        val data2 = db.runQuery { EntityDsl.insert(m).single(data) }
        db.runQuery {
            EntityDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            EntityDsl.from(m).first { m.addressId eq 16u }
        }
        assertEquals(16u, data3.addressId)
        assertEquals(UIntVersion(1u), data3.version)
    }

    @Test
    fun unsignedShort_maxValue() {
        val m = UShortTest.meta
        val data = UShortTest(1, UShort.MAX_VALUE)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null() {
        val m = UShortTest.meta
        val data = UShortTest(1, null)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid() {
        val m = UUIDTest.meta
        val value = UUID.randomUUID()
        val data = UUIDTest(1, value)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null() {
        val m = UUIDTest.meta
        val data = UUIDTest(1, null)
        db.runQuery { EntityDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            EntityDsl.from(m).first { m.id eq 1 }
        }
        assertEquals(data, data2)
    }
}
