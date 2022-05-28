package integration.core

import org.komapper.annotation.EnumType
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
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
data class AnyData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Any?)

@KomapperEntity
@KomapperTable("big_decimal_test")
data class BigDecimalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigDecimal?)

@KomapperEntity
@KomapperTable("big_integer_test")
data class BigIntegerData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigInteger?)

@KomapperEntity
@KomapperTable("boolean_test")
data class BooleanData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Boolean?)

@KomapperEntity
@KomapperTable("byte_test")
data class ByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Byte?)

@KomapperEntity
@KomapperTable("byte_array_test")
@Suppress("ArrayInDataClass")
data class ByteArrayData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: ByteArray?)

@KomapperEntity
@KomapperTable("double_test")
data class DoubleData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Double?)

@KomapperEntity
@KomapperTable("duration_test")
data class DurationData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Duration?)

enum class Direction {
    NORTH, SOUTH, EAST, WEST
}

@KomapperEntity
@KomapperTable("enum_test")
data class EnumData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Direction?)

@KomapperEntity
@KomapperTable("enum_ordinal_test")
data class EnumOrdinalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) @KomapperEnum(EnumType.ORDINAL) val value: Direction?)

@KomapperEntity
@KomapperTable("float_test")
data class FloatData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Float?)

@KomapperEntity
@KomapperTable("instant_test")
data class InstantData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Instant?)

@KomapperEntity
@KomapperTable("int_test")
data class IntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Int?)

@KomapperEntity
@KomapperTable("local_date_time_test")
data class LocalDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDateTime?)

@KomapperEntity
@KomapperTable("local_date_test")
data class LocalDateData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDate?)

@KomapperEntity
@KomapperTable("local_time_test")
data class LocalTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalTime?)

@KomapperEntity
@KomapperTable("long_test")
data class LongData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Long?)

@KomapperEntity
@KomapperTable("offset_date_time_test")
data class OffsetDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: OffsetDateTime?)

@KomapperEntity
@KomapperTable("period_test")
data class PeriodData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Period?)

@KomapperEntity
@KomapperTable("short_test")
data class ShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Short?)

@KomapperEntity
@KomapperTable("string_test")
data class StringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: String?)

@KomapperEntity
@KomapperTable("short_test")
data class UByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UByte?)

@KomapperEntity
@KomapperTable("long_test")
data class UIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UInt?)

@KomapperEntity
@KomapperTable("int_test")
data class UShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UShort?)

@KomapperEntity
@KomapperTable("uuid_test")
data class UUIDData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UUID?)

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
