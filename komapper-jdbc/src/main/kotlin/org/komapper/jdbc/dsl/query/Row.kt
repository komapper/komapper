package org.komapper.jdbc.dsl.query

import org.komapper.jdbc.JdbcDialect
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.ResultSet
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Row {
    fun asAny(index: Int): Any?
    fun asAny(columnLabel: String): Any?
    fun asArray(index: Int): Array?
    fun asArray(columnLabel: String): Array?
    fun asBigDecimal(index: Int): BigDecimal?
    fun asBigDecimal(columnLabel: String): BigDecimal?
    fun asBigInteger(index: Int): BigInteger?
    fun asBigInteger(columnLabel: String): BigInteger?
    fun asBlob(index: Int): Blob?
    fun asBlob(columnLabel: String): Blob?
    fun asBoolean(index: Int): Boolean?
    fun asBoolean(columnLabel: String): Boolean?
    fun asByte(index: Int): Byte?
    fun asByte(columnLabel: String): Byte?
    fun asByteArray(index: Int): ByteArray?
    fun asByteArray(columnLabel: String): ByteArray?
    fun asClob(index: Int): Clob?
    fun asClob(columnLabel: String): Clob?
    fun asDouble(index: Int): Double?
    fun asDouble(columnLabel: String): Double?
    fun asFloat(index: Int): Float?
    fun asFloat(columnLabel: String): Float?
    fun asInt(index: Int): Int?
    fun asInt(columnLabel: String): Int?
    fun asLocalDateTime(index: Int): LocalDateTime?
    fun asLocalDateTime(columnLabel: String): LocalDateTime?
    fun asLocalDate(index: Int): LocalDate?
    fun asLocalDate(columnLabel: String): LocalDate?
    fun asLocalTime(index: Int): LocalTime?
    fun asLocalTime(columnLabel: String): LocalTime?
    fun asLong(index: Int): Long?
    fun asLong(columnLabel: String): Long?
    fun asNClob(index: Int): NClob?
    fun asNClob(columnLabel: String): NClob?
    fun asOffsetDateTime(index: Int): OffsetDateTime?
    fun asOffsetDateTime(columnLabel: String): OffsetDateTime?
    fun asShort(index: Int): Short?
    fun asShort(columnLabel: String): Short?
    fun asString(index: Int): String?
    fun asString(columnLabel: String): String?
    fun asSQLXML(index: Int): SQLXML?
    fun asSQLXML(columnLabel: String): SQLXML?
    fun asUUID(index: Int): UUID?
    fun asUUID(columnLabel: String): UUID?
    fun <T : Any> asT(index: Int, klass: KClass<T>): T?
    fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T?
}

class RowImpl(
    private val dialect: JdbcDialect,
    private val rs: ResultSet
) : Row {

    override fun asAny(index: Int): Any? {
        return asT(index, Any::class)
    }

    override fun asAny(columnLabel: String): Any? {
        return asT(columnLabel, Any::class)
    }

    override fun asArray(index: Int): Array? {
        return asT(index, Array::class)
    }

    override fun asArray(columnLabel: String): Array? {
        return asT(columnLabel, Array::class)
    }

    override fun asBigDecimal(index: Int): BigDecimal? {
        return asT(index, BigDecimal::class)
    }

    override fun asBigDecimal(columnLabel: String): BigDecimal? {
        return asT(columnLabel, BigDecimal::class)
    }

    override fun asBigInteger(index: Int): BigInteger? {
        return asT(index, BigInteger::class)
    }

    override fun asBigInteger(columnLabel: String): BigInteger? {
        return asT(columnLabel, BigInteger::class)
    }

    override fun asBlob(index: Int): Blob? {
        return asT(index, Blob::class)
    }

    override fun asBlob(columnLabel: String): Blob? {
        return asT(columnLabel, Blob::class)
    }

    override fun asBoolean(index: Int): Boolean? {
        return asT(index, Boolean::class)
    }

    override fun asBoolean(columnLabel: String): Boolean? {
        return asT(columnLabel, Boolean::class)
    }

    override fun asByte(index: Int): Byte? {
        return asT(index, Byte::class)
    }

    override fun asByte(columnLabel: String): Byte? {
        return asT(columnLabel, Byte::class)
    }

    override fun asByteArray(index: Int): ByteArray? {
        return asT(index, ByteArray::class)
    }

    override fun asByteArray(columnLabel: String): ByteArray? {
        return asT(columnLabel, ByteArray::class)
    }

    override fun asClob(index: Int): Clob? {
        return asT(index, Clob::class)
    }

    override fun asClob(columnLabel: String): Clob? {
        return asT(columnLabel, Clob::class)
    }

    override fun asDouble(index: Int): Double? {
        return asT(index, Double::class)
    }

    override fun asDouble(columnLabel: String): Double? {
        return asT(columnLabel, Double::class)
    }

    override fun asFloat(index: Int): Float? {
        return asT(index, Float::class)
    }

    override fun asFloat(columnLabel: String): Float? {
        return asT(columnLabel, Float::class)
    }

    override fun asInt(index: Int): Int? {
        return asT(index, Int::class)
    }

    override fun asInt(columnLabel: String): Int? {
        return asT(columnLabel, Int::class)
    }

    override fun asLocalDateTime(index: Int): LocalDateTime? {
        return asT(index, LocalDateTime::class)
    }

    override fun asLocalDateTime(columnLabel: String): LocalDateTime? {
        return asT(columnLabel, LocalDateTime::class)
    }

    override fun asLocalDate(index: Int): LocalDate? {
        return asT(index, LocalDate::class)
    }

    override fun asLocalDate(columnLabel: String): LocalDate? {
        return asT(columnLabel, LocalDate::class)
    }

    override fun asLocalTime(index: Int): LocalTime? {
        return asT(index, LocalTime::class)
    }

    override fun asLocalTime(columnLabel: String): LocalTime? {
        return asT(columnLabel, LocalTime::class)
    }

    override fun asLong(index: Int): Long? {
        return asT(index, Long::class)
    }

    override fun asLong(columnLabel: String): Long? {
        return asT(columnLabel, Long::class)
    }

    override fun asNClob(index: Int): NClob? {
        return asT(index, NClob::class)
    }

    override fun asNClob(columnLabel: String): NClob? {
        return asT(columnLabel, NClob::class)
    }

    override fun asOffsetDateTime(index: Int): OffsetDateTime? {
        return asT(index, OffsetDateTime::class)
    }

    override fun asOffsetDateTime(columnLabel: String): OffsetDateTime? {
        return asT(columnLabel, OffsetDateTime::class)
    }

    override fun asShort(index: Int): Short? {
        return asT(index, Short::class)
    }

    override fun asShort(columnLabel: String): Short? {
        return asT(columnLabel, Short::class)
    }

    override fun asString(index: Int): String? {
        return asT(index, String::class)
    }

    override fun asString(columnLabel: String): String? {
        return asT(columnLabel, String::class)
    }

    override fun asSQLXML(index: Int): SQLXML? {
        return asT(index, SQLXML::class)
    }

    override fun asSQLXML(columnLabel: String): SQLXML? {
        return asT(columnLabel, SQLXML::class)
    }

    override fun asUUID(index: Int): UUID? {
        return asT(index, UUID::class)
    }

    override fun asUUID(columnLabel: String): UUID? {
        return asT(columnLabel, UUID::class)
    }

    override fun <T : Any> asT(index: Int, klass: KClass<T>): T? {
        return dialect.getValue(rs, index, klass)?.let { klass.cast(it) }
    }

    override fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T? {
        return dialect.getValue(rs, columnLabel, klass)?.let { klass.cast(it) }
    }
}
