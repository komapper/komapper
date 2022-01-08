package integration

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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@KomapperEntity
@KomapperTable("ANY_PERSON")
data class AnyPerson(@KomapperId val name: String) : Serializable

@KomapperEntity
@KomapperTable("ANY_TEST")
data class AnyTest(@KomapperId val id: Int, val value: Any)

@KomapperEntity
@KomapperTable("ARRAY_TEST")
data class ArrayTest(@KomapperId val id: Int, val value: Array?)

@KomapperEntity
@KomapperTable("BIG_DECIMAL_TEST")
data class BigDecimalTest(@KomapperId val id: Int, val value: BigDecimal)

@KomapperEntity
@KomapperTable("BIG_INTEGER_TEST")
data class BigIntegerTest(@KomapperId val id: Int, val value: BigInteger)

@KomapperEntity
@KomapperTable("BOOLEAN_TEST")
data class BooleanTest(@KomapperId val id: Int, val value: Boolean)

@KomapperEntity
@KomapperTable("BYTE_TEST")
data class ByteTest(@KomapperId val id: Int, val value: Byte)

@KomapperEntity
@KomapperTable("BYTE_ARRAY_TEST")
@Suppress("ArrayInDataClass")
data class ByteArrayTest(@KomapperId val id: Int, val value: ByteArray)

@KomapperEntity
@KomapperTable("DOUBLE_TEST")
data class DoubleTest(@KomapperId val id: Int, val value: Double)

enum class Direction {
    NORTH, SOUTH, EAST, WEST
}

@KomapperEntity
@KomapperTable("ENUM_TEST")
data class EnumTest(@KomapperId val id: Int, val value: Direction)

@KomapperEntity
@KomapperTable("FLOAT_TEST")
data class FloatTest(@KomapperId val id: Int, val value: Float)

@KomapperEntity
@KomapperTable("INT_TEST")
data class IntTest(@KomapperId val id: Int, val value: Int)

@KomapperEntity
@KomapperTable("LOCAL_DATE_TIME_TEST")
data class LocalDateTimeTest(@KomapperId val id: Int, val value: LocalDateTime)

@KomapperEntity
@KomapperTable("LOCAL_DATE_TEST")
data class LocalDateTest(@KomapperId val id: Int, val value: LocalDate)

@KomapperEntity
@KomapperTable("LOCAL_TIME_TEST")
data class LocalTimeTest(@KomapperId val id: Int, val value: LocalTime)

@KomapperEntity
@KomapperTable("LONG_TEST")
data class LongTest(@KomapperId val id: Int, val value: Long)

@KomapperEntity
@KomapperTable("OFFSET_DATE_TIME_TEST")
data class OffsetDateTimeTest(@KomapperId val id: Int, val value: OffsetDateTime)

@KomapperEntity
@KomapperTable("SHORT_TEST")
data class ShortTest(@KomapperId val id: Int, val value: Short)

@KomapperEntity
@KomapperTable("SQLXML_TEST")
data class SqlXmlTest(@KomapperId val id: Int, val value: SQLXML?)

@KomapperEntity
@KomapperTable("STRING_TEST")
data class StringTest(@KomapperId val id: Int, val value: String)

data class Json(val data: String)

@KomapperEntity
@KomapperTable("JSON_TEST")
data class JsonTest(@KomapperId val id: Int, val value: Json)

@KomapperEntity
@KomapperTable("SHORT_TEST")
data class UByteTest(@KomapperId val id: Int, val value: UByte?)

@KomapperEntity
@KomapperTable("LONG_TEST")
data class UIntTest(@KomapperId val id: Int, val value: UInt?)

@KomapperEntity
@KomapperTable("INT_TEST")
data class UShortTest(@KomapperId val id: Int, val value: UShort?)

@KomapperEntity
@KomapperTable("UUID_TEST")
data class UUIDTest(@KomapperId val id: Int, val value: UUID?)

@KomapperEntity
@KomapperTable("ADDRESS")
data class UnsignedAddress(
    @KomapperId @KomapperColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UInt?
)

@JvmInline
value class UIntVersion(val value: UInt)

@KomapperEntity
@KomapperTable("ADDRESS")
data class UnsignedAddress2(
    @KomapperId @KomapperColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UIntVersion?
)

@KomapperEntity
@KomapperTable("IDENTITY_STRATEGY")
data class UnsignedIdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: UInt?,
    val value: String
)

@KomapperEntity
@KomapperTable("SEQUENCE_STRATEGY")
data class UnsignedSequenceStrategy(
    @KomapperId @KomapperSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: UInt,
    val value: String
)
