package integration

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmSequence
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmVersion
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

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

@KmEntity
@KmTable("BIG_DECIMAL_TEST")
data class BigDecimalTest(@KmId val id: Int, val value: BigDecimal) {
    companion object
}

@KmEntity
@KmTable("BIG_INTEGER_TEST")
data class BigIntegerTest(@KmId val id: Int, val value: BigInteger) {
    companion object
}

@KmEntity
@KmTable("BOOLEAN_TEST")
data class BooleanTest(@KmId val id: Int, val value: Boolean) {
    companion object
}

@KmEntity
@KmTable("BYTE_TEST")
data class ByteTest(@KmId val id: Int, val value: Byte) {
    companion object
}

@KmEntity
@KmTable("BYTE_ARRAY_TEST")
@Suppress("ArrayInDataClass")
data class ByteArrayTest(@KmId val id: Int, val value: ByteArray) {
    companion object
}

@KmEntity
@KmTable("DOUBLE_TEST")
data class DoubleTest(@KmId val id: Int, val value: Double) {
    companion object
}

@KmEntity
@KmTable("FLOAT_TEST")
data class FloatTest(@KmId val id: Int, val value: Float) {
    companion object
}

@KmEntity
@KmTable("INT_TEST")
data class IntTest(@KmId val id: Int, val value: Int) {
    companion object
}

@KmEntity
@KmTable("LOCAL_DATE_TIME_TEST")
data class LocalDateTimeTest(@KmId val id: Int, val value: LocalDateTime) {
    companion object
}

@KmEntity
@KmTable("LOCAL_DATE_TEST")
data class LocalDateTest(@KmId val id: Int, val value: LocalDate) {
    companion object
}

@KmEntity
@KmTable("LOCAL_TIME_TEST")
data class LocalTimeTest(@KmId val id: Int, val value: LocalTime) {
    companion object
}

@KmEntity
@KmTable("LONG_TEST")
data class LongTest(@KmId val id: Int, val value: Long) {
    companion object
}

@KmEntity
@KmTable("OFFSET_DATE_TIME_TEST")
data class OffsetDateTimeTest(@KmId val id: Int, val value: OffsetDateTime) {
    companion object
}

@KmEntity
@KmTable("SHORT_TEST")
data class ShortTest(@KmId val id: Int, val value: Short) {
    companion object
}

@KmEntity
@KmTable("STRING_TEST")
data class StringTest(@KmId val id: Int, val value: String) {
    companion object
}

data class Json(val data: String)

@KmEntity
@KmTable("JSON_TEST")
data class JsonTest(@KmId val id: Int, val value: Json) {
    companion object
}

@KmEntity
@KmTable("SHORT_TEST")
data class UByteTest(@KmId val id: Int, val value: UByte?) {
    companion object
}

@KmEntity
@KmTable("LONG_TEST")
data class UIntTest(@KmId val id: Int, val value: UInt?) {
    companion object
}

@KmEntity
@KmTable("INT_TEST")
data class UShortTest(@KmId val id: Int, val value: UShort?) {
    companion object
}

@KmEntity
@KmTable("UUID_TEST")
data class UUIDTest(@KmId val id: Int, val value: UUID?) {
    companion object
}

@KmEntity
@KmTable("ADDRESS")
data class UnsignedAddress(
    @KmId @KmColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KmVersion val version: UInt?
) {
    companion object
}

@JvmInline
value class UIntVersion(val value: UInt)

@KmEntity
@KmTable("ADDRESS")
data class UnsignedAddress2(
    @KmId @KmColumn(name = "ADDRESS_ID") val addressId: UInt,
    val street: String,
    @KmVersion val version: UIntVersion?
) {
    companion object
}

@KmEntity
@KmTable("IDENTITY_STRATEGY")
data class UnsignedIdentityStrategy(
    @KmId @KmAutoIncrement val id: UInt?,
    val value: String
) {
    companion object
}

@KmEntity
@KmTable("SEQUENCE_STRATEGY")
data class UnsignedSequenceStrategy(
    @KmId @KmSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: UInt,
    val value: String
) {
    companion object
}
