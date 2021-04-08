package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmTable
import org.komapper.core.dsl.EntityQuery
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(Env::class)
class DataTypeTest(val db: Database) {

    @KmEntity
    @KmTable("ANY_PERSON")
    data class AnyPerson(@KmId val name: String) : Serializable {
        companion object
    }

    @KmEntity
    @KmTable("ANY_TEST")
    data class AnyTest(@KmId val id: Int, val value: Any) {
        companion object
    }

    @Test
    fun any() {
        val m = AnyTest.metamodel()
        val data = AnyTest(
            1,
            AnyPerson("ABC")
        )
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("BIG_DECIMAL_TEST")
    data class BigDecimalTest(@KmId val id: Int, val value: BigDecimal) {
        companion object
    }

    @Test
    fun bigDecimal() {
        val m = BigDecimalTest.metamodel()
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("BIG_INTEGER_TEST")
    data class BigIntegerTest(@KmId val id: Int, val value: BigInteger) {
        companion object
    }

    @Test
    fun bigInteger() {
        val m = BigIntegerTest.metamodel()
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("BOOLEAN_TEST")
    data class BooleanTest(@KmId val id: Int, val value: Boolean) {
        companion object
    }

    @Test
    fun boolean() {
        val m = BooleanTest.metamodel()
        val data = BooleanTest(1, true)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("BYTE_TEST")
    data class ByteTest(@KmId val id: Int, val value: Byte) {
        companion object
    }

    @Test
    fun byte() {
        val m = ByteTest.metamodel()
        val data = ByteTest(1, 10)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("BYTE_ARRAY_TEST")
    @Suppress("ArrayInDataClass")
    data class ByteArrayTest(@KmId val id: Int, val value: ByteArray) {
        companion object
    }

    @Test
    fun byteArray() {
        val m = ByteArrayTest.metamodel()
        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data.id, data2.id)
        Assertions.assertArrayEquals(data.value, data2.value)
    }

    @KmEntity
    @KmTable("DOUBLE_TEST")
    data class DoubleTest(@KmId val id: Int, val value: Double) {
        companion object
    }

    @Test
    fun double() {
        val m = DoubleTest.metamodel()
        val data = DoubleTest(1, 10.0)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("FLOAT_TEST")
    data class FloatTest(@KmId val id: Int, val value: Float) {
        companion object
    }

    @Test
    fun float() {
        val m = FloatTest.metamodel()
        val data = FloatTest(1, 10.0f)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("INT_TEST")
    data class IntTest(@KmId val id: Int, val value: Int) {
        companion object
    }

    @Test
    fun int() {
        val m = IntTest.metamodel()
        val data = IntTest(1, 10)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("LOCAL_DATE_TIME_TEST")
    data class LocalDateTimeTest(@KmId val id: Int, val value: LocalDateTime) {
        companion object
    }

    @Test
    fun localDateTime() {
        val m = LocalDateTimeTest.metamodel()
        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("LOCAL_DATE_TEST")
    data class LocalDateTest(@KmId val id: Int, val value: LocalDate) {
        companion object
    }

    @Test
    fun localDate() {
        val m = LocalDateTest.metamodel()
        val data = LocalDateTest(
            1,
            LocalDate.of(2019, 6, 1)
        )
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("LOCAL_TIME_TEST")
    data class LocalTimeTest(@KmId val id: Int, val value: LocalTime) {
        companion object
    }

    @Test
    fun localTime() {
        val m = LocalTimeTest.metamodel()
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("LONG_TEST")
    data class LongTest(@KmId val id: Int, val value: Long) {
        companion object
    }

    @Test
    fun long() {
        val m = LongTest.metamodel()
        val data = LongTest(1, 10L)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("OFFSET_DATE_TIME_TEST")
    data class OffsetDateTimeTest(@KmId val id: Int, val value: OffsetDateTime) {
        companion object
    }

    @Test
    fun offsetDateTime() {
        val m = OffsetDateTimeTest.metamodel()
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("SHORT_TEST")
    data class ShortTest(@KmId val id: Int, val value: Short) {
        companion object
    }

    @Test
    fun short() {
        val m = ShortTest.metamodel()
        val data = ShortTest(1, 10)
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }

    @KmEntity
    @KmTable("STRING_TEST")
    data class StringTest(@KmId val id: Int, val value: String) {
        companion object
    }

    @Test
    fun string() {
        val m = StringTest.metamodel()
        val data = StringTest(1, "ABC")
        db.execute { EntityQuery.insert(m, data) }
        val data2 = db.execute {
            EntityQuery.first(m).where { m.id eq 1 }
        }
        Assertions.assertEquals(data, data2)
    }
}
