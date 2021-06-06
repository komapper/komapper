package org.komapper.core.dsl.query

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Note that columns are numbered from 0.
 */
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
