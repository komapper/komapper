package integration.core

import integration.core.enumclass.Color
import integration.core.enumclass.Direction
import integration.core.enumclass.Mood
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
public data class AnyPerson(@KomapperId val name: String) : Serializable

@KomapperEntity
@KomapperTable("any_data")
public data class AnyData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Any?)

@KomapperEntity
@KomapperTable("big_decimal_data")
public data class BigDecimalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigDecimal?)

@KomapperEntity
@KomapperTable("big_integer_data")
public data class BigIntegerData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: BigInteger?)

@KomapperEntity
@KomapperTable("boolean_data")
public data class BooleanData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Boolean?)

@KomapperEntity
@KomapperTable("byte_data")
public data class ByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Byte?)

@KomapperEntity
@KomapperTable("byte_array_data")
@Suppress("ArrayInDataClass")
public data class ByteArrayData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: ByteArray?)

@KomapperEntity
@KomapperTable("double_data")
public data class DoubleData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Double?)

@KomapperEntity
@KomapperTable("duration_data")
public data class DurationData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Duration?)

@KomapperEntity
@KomapperTable("enum_data")
public data class EnumData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Direction?)

@KomapperEntity
@KomapperTable("enum_ordinal_data")
public data class EnumOrdinalData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true)
    @KomapperEnum(EnumType.ORDINAL)
    val value: Direction?,
)

@KomapperEntity
@KomapperTable("enum_property_data")
public data class EnumPropertyData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true)
    @KomapperEnum(EnumType.PROPERTY, hint = "value")
    val value: Color?,
)

@KomapperEntity
@KomapperTable("enum_udt_data")
public data class EnumUdtData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true)
    @KomapperEnum(EnumType.TYPE)
    val value: Mood?,
)

public data class DirectionInfo(
    val direction: Direction,
)

public data class ColorInfo(
    val color: Color,
)

@KomapperEntity
@KomapperTable("enum_ordinal_data")
public data class EmbeddedEnumOrdinalData(
    @KomapperId val id: Int,
    @KomapperEmbedded
    @KomapperColumnOverride(name = "direction", KomapperColumn(name = "value", alwaysQuote = true))
    @KomapperEnumOverride(name = "direction", KomapperEnum(EnumType.ORDINAL))
    val value: DirectionInfo?,
)

@KomapperEntity
@KomapperTable("enum_property_data")
public data class EmbeddedEnumPropertyData(
    @KomapperId val id: Int,
    @KomapperEmbedded
    @KomapperColumnOverride(name = "color", KomapperColumn(name = "value", alwaysQuote = true))
    @KomapperEnumOverride(name = "color", KomapperEnum(EnumType.PROPERTY, "value"))
    val value: ColorInfo?,
)

@KomapperEntity
@KomapperTable("float_data")
public data class FloatData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Float?)

@KomapperEntity
@KomapperTable("instant_data")
public data class InstantData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Instant?)

@KomapperEntity
@KomapperTable("int_data")
public data class IntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Int?)

@KomapperEntity
@KomapperTable("local_date_time_data")
public data class LocalDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDateTime?)

@KomapperEntity
@KomapperTable("local_date_data")
public data class LocalDateData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDate?)

@KomapperEntity
@KomapperTable("local_time_data")
public data class LocalTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalTime?)

@KomapperEntity
@KomapperTable("long_data")
public data class LongData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Long?)

@KomapperEntity
@KomapperTable("offset_date_time_data")
public data class OffsetDateTimeData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: OffsetDateTime?)

@KomapperEntity
@KomapperTable("string_data")
public data class PairOfStringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Pair<String, String>?)

@KomapperEntity
@KomapperTable("string_data")
public data class PairOfIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Pair<Int, Int>?)

@KomapperEntity
@KomapperTable("period_data")
public data class PeriodData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Period?)

@KomapperEntity
@KomapperTable("short_data")
public data class ShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Short?)

@KomapperEntity
@KomapperTable("string_data")
public data class StringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: String?)

@KomapperEntity
@KomapperTable("short_data")
public data class UByteData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UByte?)

@KomapperEntity
@KomapperTable("long_data")
public data class UIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UInt?)

@KomapperEntity
@KomapperTable("int_data")
public data class UShortData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UShort?)

@KomapperEntity
@KomapperTable("uuid_data")
public data class UUIDData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UUID?)

@KomapperEntity
@KomapperTable("address")
public data class UnsignedAddress(
    @KomapperId
    @KomapperColumn(name = "address_id")
    val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UInt?,
)

@JvmInline
public value class UIntVersion(@KomapperColumn(alwaysQuote = true) public val value: UInt)

@KomapperEntity
@KomapperTable("address")
public data class UnsignedAddress2(
    @KomapperId
    @KomapperColumn(name = "address_id")
    val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UIntVersion?,
)

@KomapperEntity
@KomapperTable("identity_strategy")
public data class UnsignedIdentityStrategy(
    @KomapperId @KomapperAutoIncrement
    val id: UInt?,
    @KomapperColumn(alwaysQuote = true) val value: String,
)

@KomapperEntity
@KomapperTable("sequence_strategy")
public data class UnsignedSequenceStrategy(
    @KomapperId
    @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100)
    val id: UInt,
    @KomapperColumn(alwaysQuote = true) val value: String,
)

@KomapperEntity
@KomapperTable("int_data")
public data class UserDefinedIntData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: UserDefinedInt?)

public data class UserDefinedInt(val value: Int)

@KomapperEntity
@KomapperTable("string_data")
public data class UserDefinedStringData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: UserDefinedString?,
)

public data class UserDefinedString(val value: String)

@KomapperEntity
@KomapperTable("string_data")
public data class WrappedStringData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: WrappedString?)

public data class WrappedString(val value: String)
