package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.runQuery
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(Env::class)
class DataTypeTest(val db: Database) {

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any() {
        val m = AnyTest.alias
        val data = AnyTest(
            1,
            AnyPerson("ABC")
        )
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigDecimal() {
        val m = BigDecimalTest.alias
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = BigIntegerTest.alias
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        val m = BooleanTest.alias
        val data = BooleanTest(1, true)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = ByteTest.alias
        val data = ByteTest(1, 10)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
        val m = ByteArrayTest.alias
        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data.id, data2.id)
        Assertions.assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double() {
        val m = DoubleTest.alias
        val data = DoubleTest(1, 10.0)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = FloatTest.alias
        val data = FloatTest(1, 10.0f)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun int() {
        val m = IntTest.alias
        val data = IntTest(1, 10)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = LocalDateTimeTest.alias
        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate() {
        val m = LocalDateTest.alias
        val data = LocalDateTest(
            1,
            LocalDate.of(2019, 6, 1)
        )
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime() {
        val m = LocalTimeTest.alias
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = LongTest.alias
        val data = LongTest(1, 10L)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun offsetDateTime() {
        val m = OffsetDateTimeTest.alias
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.H2])
    @Test
    fun offsetDateTime_unlessH2() {
        val m = OffsetDateTimeTest.alias
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertNotNull(data2)
    }

    @Test
    fun short() {
        val m = ShortTest.alias
        val data = ShortTest(1, 10)
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Test
    fun string() {
        val m = StringTest.alias
        val data = StringTest(1, "ABC")
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun json_postgresql() {
        val m = JsonTest.alias
        val data = JsonTest(
            1,
            Json(
                """
            {"a": 100, "b": "Hello"}
                """.trimIndent()
            )
        )
        db.runQuery { EntityDsl.insert(m, data) }
        val data2 = db.runQuery {
            EntityDsl.first(m) { m.id eq 1 }
        }
        assertEquals(data, data2)

        val result = db.runQuery {
            TemplateDsl
                .from("select value->'b' as x from json_test")
                .select { asT("x", Json::class)!! }
                .first()
        }
        assertEquals("\"Hello\"", result.data)
    }
}
