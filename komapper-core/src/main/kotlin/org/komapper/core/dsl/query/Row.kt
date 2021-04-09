package org.komapper.core.dsl.query

import org.komapper.core.Dialect
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
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Row {
    fun asAny(columnLabel: String): Any?
    fun asArray(columnLabel: String): Array?
    fun asBigDecimal(columnLabel: String): BigDecimal?
    fun asBigInteger(columnLabel: String): BigInteger?
    fun asBlob(columnLabel: String): Blob?
    fun asBoolean(columnLabel: String): Boolean?
    fun asByte(columnLabel: String): Byte?
    fun asByteArray(columnLabel: String): ByteArray?
    fun asClob(columnLabel: String): Clob?
    fun asDouble(columnLabel: String): Double?
    fun asFloat(columnLabel: String): Float?
    fun asInt(columnLabel: String): Int?
    fun asLocalDateTime(columnLabel: String): LocalDateTime?
    fun asLocalDate(columnLabel: String): LocalDate?
    fun asLocalTime(columnLabel: String): LocalTime?
    fun asLong(columnLabel: String): Long?
    fun asNClob(columnLabel: String): NClob?
    fun asOffsetDateTime(columnLabel: String): OffsetDateTime?
    fun asShort(columnLabel: String): Short?
    fun asString(columnLabel: String): String?
    fun asSQLXML(columnLabel: String): SQLXML?
    fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T?
}

class RowImpl(
    private val dialect: Dialect,
    private val rs: ResultSet
) : Row {

    override fun asAny(columnLabel: String): Any? {
        return asT(columnLabel, Any::class)
    }

    override fun asArray(columnLabel: String): Array? {
        return asT(columnLabel, Array::class)
    }

    override fun asBigDecimal(columnLabel: String): BigDecimal? {
        return asT(columnLabel, BigDecimal::class)
    }

    override fun asBigInteger(columnLabel: String): BigInteger? {
        return asT(columnLabel, BigInteger::class)
    }

    override fun asBlob(columnLabel: String): Blob? {
        return asT(columnLabel, Blob::class)
    }

    override fun asBoolean(columnLabel: String): Boolean? {
        return asT(columnLabel, Boolean::class)
    }

    override fun asByte(columnLabel: String): Byte? {
        return asT(columnLabel, Byte::class)
    }

    override fun asByteArray(columnLabel: String): ByteArray? {
        return asT(columnLabel, ByteArray::class)
    }

    override fun asClob(columnLabel: String): Clob? {
        return asT(columnLabel, Clob::class)
    }

    override fun asDouble(columnLabel: String): Double? {
        return asT(columnLabel, Double::class)
    }

    override fun asFloat(columnLabel: String): Float? {
        return asT(columnLabel, Float::class)
    }

    override fun asInt(columnLabel: String): Int? {
        return asT(columnLabel, Int::class)
    }

    override fun asLocalDateTime(columnLabel: String): LocalDateTime? {
        return asT(columnLabel, LocalDateTime::class)
    }

    override fun asLocalDate(columnLabel: String): LocalDate? {
        return asT(columnLabel, LocalDate::class)
    }

    override fun asLocalTime(columnLabel: String): LocalTime? {
        return asT(columnLabel, LocalTime::class)
    }

    override fun asLong(columnLabel: String): Long? {
        return asT(columnLabel, Long::class)
    }

    override fun asNClob(columnLabel: String): NClob? {
        return asT(columnLabel, NClob::class)
    }

    override fun asOffsetDateTime(columnLabel: String): OffsetDateTime? {
        return asT(columnLabel, OffsetDateTime::class)
    }

    override fun asShort(columnLabel: String): Short? {
        return asT(columnLabel, Short::class)
    }

    override fun asString(columnLabel: String): String? {
        return asT(columnLabel, String::class)
    }

    override fun asSQLXML(columnLabel: String): SQLXML? {
        return asT(columnLabel, SQLXML::class)
    }

    override fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T? {
        return dialect.getValue(rs, columnLabel, klass)?.let { klass.cast(it) }
    }
}
