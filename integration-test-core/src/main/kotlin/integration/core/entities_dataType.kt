package integration.core

import integration.core.enumclass.Direction
import org.komapper.annotation.EnumType
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
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
@KomapperTable("any_data")
data class AnyData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Any?)

@KomapperEntity
@KomapperTable("big_decimal_data")
data class BigDecimalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigDecimal?)

@KomapperEntity
@KomapperTable("big_integer_data")
data class BigIntegerData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigInteger?)

@KomapperEntity
@KomapperTable("boolean_data")
data class BooleanData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Boolean?)

@KomapperEntity
@KomapperTable("byte_data")
data class ByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Byte?)

@KomapperEntity
@KomapperTable("byte_array_data")
@Suppress("ArrayInDataClass")
data class ByteArrayData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: ByteArray?)

@KomapperEntity
@KomapperTable("double_data")
data class DoubleData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Double?)

@KomapperEntity
@KomapperTable("duration_data")
data class DurationData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Duration?)

@KomapperEntity
@KomapperTable("enum_data")
data class EnumData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Direction?)

@KomapperEntity
@KomapperTable("enum_ordinal_data")
data class EnumOrdinalData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) @KomapperEnum(EnumType.ORDINAL) val value: Direction?
)

data class DirectionInfo(
    val direction: Direction
)

@KomapperEntity
@KomapperTable("enum_ordinal_data")
data class EmbeddedEnumOrdinalData(
    @KomapperId val id: Int,
    @KomapperEmbedded
    @KomapperColumnOverride(name = "direction", KomapperColumn(name = "value", alwaysQuote = true))
    @KomapperEnumOverride(name = "direction", KomapperEnum(EnumType.ORDINAL))
    val value: DirectionInfo?
)

@KomapperEntity
@KomapperTable("float_data")
data class FloatData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Float?)

@KomapperEntity
@KomapperTable("instant_data")
data class InstantData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Instant?)

@KomapperEntity
@KomapperTable("int_data")
data class IntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Int?)

@KomapperEntity
@KomapperTable("local_date_time_data")
data class LocalDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDateTime?)

@KomapperEntity
@KomapperTable("local_date_data")
data class LocalDateData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDate?)

@KomapperEntity
@KomapperTable("local_time_data")
data class LocalTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalTime?)

@KomapperEntity
@KomapperTable("long_data")
data class LongData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Long?)

@KomapperEntity
@KomapperTable("offset_date_time_data")
data class OffsetDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: OffsetDateTime?)

@KomapperEntity
@KomapperTable("period_data")
data class PeriodData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Period?)

@KomapperEntity
@KomapperTable("short_data")
data class ShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Short?)

@KomapperEntity
@KomapperTable("string_data")
data class StringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: String?)

@KomapperEntity
@KomapperTable("short_data")
data class UByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UByte?)

@KomapperEntity
@KomapperTable("long_data")
data class UIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UInt?)

@KomapperEntity
@KomapperTable("int_data")
data class UShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UShort?)

@KomapperEntity
@KomapperTable("uuid_data")
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

@KomapperEntity
@KomapperTable("int_data")
data class UserDefinedIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UserDefinedInt?)

data class UserDefinedInt(val value: Int)

@KomapperEntity
@KomapperTable("string_data")
data class UserDefinedStringData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: UserDefinedString?
)

data class UserDefinedString(val value: String)

@KomapperEntity
@KomapperTable("string_data")
data class WrappedStringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: WrappedString?)

data class WrappedString(val value: String)
