package integration.core

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.SQLXML
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.Period
import java.util.UUID

@KomapperEntity
@KomapperTable("any_person")
data class AnyPerson(@KomapperId val name: String) : Serializable

@KomapperEntity
@KomapperTable("any_test")
data class AnyTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Any)

@KomapperEntity
@KomapperTable("array_test")
data class ArrayTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Array?)

@KomapperEntity
@KomapperTable("big_decimal_test")
data class BigDecimalTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigDecimal)

@KomapperEntity
@KomapperTable("big_integer_test")
data class BigIntegerTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigInteger)

@KomapperEntity
@KomapperTable("boolean_test")
data class BooleanTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Boolean)

@KomapperEntity
@KomapperTable("byte_test")
data class ByteTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Byte)

@KomapperEntity
@KomapperTable("byte_array_test")
@Suppress("ArrayInDataClass")
data class ByteArrayTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: ByteArray)

@KomapperEntity
@KomapperTable("double_test")
data class DoubleTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Double)

@KomapperEntity
@KomapperTable("duration_test")
data class DurationTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Duration)

enum class Direction {
    NORTH, SOUTH, EAST, WEST
}

@KomapperEntity
@KomapperTable("enum_test")
data class EnumTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Direction)

@KomapperEntity
@KomapperTable("float_test")
data class FloatTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Float)

@KomapperEntity
@KomapperTable("instant_test")
data class InstantTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Instant?)

@KomapperEntity
@KomapperTable("int_test")
data class IntTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Int)

@KomapperEntity
@KomapperTable("local_date_time_test")
data class LocalDateTimeTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDateTime)

@KomapperEntity
@KomapperTable("local_date_test")
data class LocalDateTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDate)

@KomapperEntity
@KomapperTable("local_time_test")
data class LocalTimeTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalTime)

@KomapperEntity
@KomapperTable("long_test")
data class LongTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Long)

@KomapperEntity
@KomapperTable("offset_date_time_test")
data class OffsetDateTimeTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: OffsetDateTime?)

@KomapperEntity
@KomapperTable("period_test")
data class PeriodTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Period)

@KomapperEntity
@KomapperTable("short_test")
data class ShortTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Short)

@KomapperEntity
@KomapperTable("sqlxml_test")
data class SqlXmlTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: SQLXML?)

@KomapperEntity
@KomapperTable("string_test")
data class StringTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: String)

@KomapperEntity
@KomapperTable("short_test")
data class UByteTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UByte?)

@KomapperEntity
@KomapperTable("long_test")
data class UIntTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UInt?)

@KomapperEntity
@KomapperTable("int_test")
data class UShortTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UShort?)

@KomapperEntity
@KomapperTable("uuid_test")
data class UUIDTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UUID?)

@KomapperEntity
@KomapperTable("address")
data class UnsignedAddress(
    @KomapperId @KomapperColumn(name = "address_id") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UInt?
)

@JvmInline
value class UIntVersion(@KomapperColumn(alwaysQuote = true) val value: UInt)

@KomapperEntity
@KomapperTable("address")
data class UnsignedAddress2(
    @KomapperId @KomapperColumn(name = "address_id") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UIntVersion?
)

@KomapperEntity
@KomapperTable("identity_strategy")
data class UnsignedIdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: UInt?,
    @KomapperColumn(alwaysQuote = true) val value: String
)

@KomapperEntity
@KomapperTable("sequence_strategy")
data class UnsignedSequenceStrategy(
    @KomapperId @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100) val id: UInt,
    @KomapperColumn(alwaysQuote = true) val value: String
)
