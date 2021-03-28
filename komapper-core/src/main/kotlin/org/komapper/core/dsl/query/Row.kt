package org.komapper.core.dsl.query

import org.komapper.core.config.Dialect
import java.math.BigDecimal
import java.math.BigInteger
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

class Row(
    private val dialect: Dialect,
    private val rs: ResultSet
) {

    fun asAny(columnLabel: String): Any? {
        return asT(columnLabel, Any::class)
    }

    fun asArray(columnLabel: String): java.sql.Array? {
        return asT(columnLabel, java.sql.Array::class)
    }

    fun asBigDecimal(columnLabel: String): BigDecimal? {
        return asT(columnLabel, BigDecimal::class)
    }

    fun asBigInteger(columnLabel: String): BigInteger? {
        return asT(columnLabel, BigInteger::class)
    }

    fun asBlob(columnLabel: String): Blob? {
        return asT(columnLabel, Blob::class)
    }

    fun asBoolean(columnLabel: String): Boolean? {
        return asT(columnLabel, Boolean::class)
    }

    fun asByte(columnLabel: String): Byte? {
        return asT(columnLabel, Byte::class)
    }

    fun asByteArray(columnLabel: String): ByteArray? {
        return asT(columnLabel, ByteArray::class)
    }

    fun asClob(columnLabel: String): Clob? {
        return asT(columnLabel, Clob::class)
    }

    fun asDouble(columnLabel: String): Double? {
        return asT(columnLabel, Double::class)
    }

    fun <E : Enum<E>> asEnum(columnLabel: String, klass: KClass<E>): E? {
        return asT(columnLabel, klass)
    }

    fun asFloat(columnLabel: String): Float? {
        return asT(columnLabel, Float::class)
    }

    fun asInt(columnLabel: String): Int? {
        return asT(columnLabel, Int::class)
    }

    fun asLocalDateTime(columnLabel: String): LocalDateTime? {
        return asT(columnLabel, LocalDateTime::class)
    }

    fun asLocalDate(columnLabel: String): LocalDate? {
        return asT(columnLabel, LocalDate::class)
    }

    fun asLocalTime(columnLabel: String): LocalTime? {
        return asT(columnLabel, LocalTime::class)
    }

    fun asLong(columnLabel: String): Long? {
        return asT(columnLabel, Long::class)
    }

    fun asNClob(columnLabel: String): NClob? {
        return asT(columnLabel, NClob::class)
    }

    fun asOffsetDateTime(columnLabel: String): OffsetDateTime? {
        return asT(columnLabel, OffsetDateTime::class)
    }

    fun asShort(columnLabel: String): Short? {
        return asT(columnLabel, Short::class)
    }

    fun asString(columnLabel: String): String? {
        return asT(columnLabel, String::class)
    }

    fun asSQLXML(columnLabel: String): SQLXML? {
        return asT(columnLabel, SQLXML::class)
    }

    private fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T? {
        return dialect.getValue(rs, columnLabel, klass)?.let { klass.cast(it) }
    }
}
