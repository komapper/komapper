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
data class AnyPerson(@KomapperId val name: String) : Serializable {
    companion object
}

@KomapperEntity
@KomapperTable("ANY_TEST")
data class AnyTest(@KomapperId val id: Int, val value: Any) {
    companion object
}

@KomapperEntity
@KomapperTable("ARRAY_TEST")
data class ArrayTest(@KomapperId val id: Int, val value: Array?) {
    companion object
}

@KomapperEntity
@KomapperTable("BIG_DECIMAL_TEST")
data class BigDecimalTest(@KomapperId val id: Int, val value: BigDecimal) {
    companion object
}

@KomapperEntity
@KomapperTable("BIG_INTEGER_TEST")
data class BigIntegerTest(@KomapperId val id: Int, val value: BigInteger) {
    companion object
}

@KomapperEntity
@KomapperTable("BOOLEAN_TEST")
data class BooleanTest(@KomapperId val id: Int, val value: Boolean) {
    companion object
}

@KomapperEntity
@KomapperTable("BYTE_TEST")
data class ByteTest(@KomapperId val id: Int, val value: Byte) {
    companion object
}

@KomapperEntity
@KomapperTable("BYTE_ARRAY_TEST")
@Suppress("ArrayInDataClass")
data class ByteArrayTest(@KomapperId val id: Int, val value: ByteArray) {
    companion object
}

@KomapperEntity
@KomapperTable("DOUBLE_TEST")
data class DoubleTest(@KomapperId val id: Int, val value: Double) {
    companion object
}

@KomapperEntity
@KomapperTable("FLOAT_TEST")
data class FloatTest(@KomapperId val id: Int, val value: Float) {
    companion object
}

@KomapperEntity
@KomapperTable("INT_TEST")
data class IntTest(@KomapperId val id: Int, val value: Int) {
    companion object
}

@KomapperEntity
@KomapperTable("LOCAL_DATE_TIME_TEST")
data class LocalDateTimeTest(@KomapperId val id: Int, val value: LocalDateTime) {
    companion object
}

@KomapperEntity
@KomapperTable("LOCAL_DATE_TEST")
data class LocalDateTest(@KomapperId val id: Int, val value: LocalDate) {
    companion object
}

@KomapperEntity
@KomapperTable("LOCAL_TIME_TEST")
data class LocalTimeTest(@KomapperId val id: Int, val value: LocalTime) {
    companion object
}

@KomapperEntity
@KomapperTable("LONG_TEST")
data class LongTest(@KomapperId val id: Int, val value: Long) {
    companion object
}

@KomapperEntity
@KomapperTable("OFFSET_DATE_TIME_TEST")
data class OffsetDateTimeTest(@KomapperId val id: Int, val value: OffsetDateTime) {
    companion object
}

@KomapperEntity
@KomapperTable("SHORT_TEST")
data class ShortTest(@KomapperId val id: Int, val value: Short) {
    companion object
}

@KomapperEntity
@KomapperTable("SQLXML_TEST")
data class SqlXmlTest(@KomapperId val id: Int, val value: SQLXML?) {
    companion object
}

@KomapperEntity
@KomapperTable("STRING_TEST")
data class StringTest(@KomapperId val id: Int, val value: String) {
    companion object
}

data class Json(val data: String)

@KomapperEntity
@KomapperTable("JSON_TEST")
data class JsonTest(@KomapperId val id: Int, val value: Json) {
    companion object
}

@KomapperEntity
@KomapperTable("SHORT_TEST")
data class UByteTest(@KomapperId val id: Int, val value: UByte?) {
    companion object
}

@KomapperEntity
@KomapperTable("LONG_TEST")
data class UIntTest(@KomapperId val id: Int, val value: UInt?) {
    companion object
}

@KomapperEntity
@KomapperTable("INT_TEST")
data class UShortTest(@KomapperId val id: Int, val value: UShort?) {
    companion object
}

@KomapperEntity
@KomapperTable("UUID_TEST")
data class UUIDTest(@KomapperId val id: Int, val value: UUID?) {
    companion object
}

@KomapperEntity
@KomapperTable("ADDRESS")
data class UnsignedAddress(
    @KomapperId @KomapperColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UInt?
) {
    companion object
}

@JvmInline
value class UIntVersion(val value: UInt)

@KomapperEntity
@KomapperTable("ADDRESS")
data class UnsignedAddress2(
    @KomapperId @KomapperColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KomapperVersion val version: UIntVersion?
) {
    companion object
}

@KomapperEntity
@KomapperTable("IDENTITY_STRATEGY")
data class UnsignedIdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: UInt?,
    val value: String
) {
    companion object
}

@KomapperEntity
@KomapperTable("SEQUENCE_STRATEGY")
data class UnsignedSequenceStrategy(
    @KomapperId @KomapperSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: UInt,
    val value: String
) {
    companion object
}
