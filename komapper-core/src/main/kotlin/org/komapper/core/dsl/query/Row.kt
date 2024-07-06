package org.komapper.core.dsl.query

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Represents a single row connected to the result set obtained by query execution.
 * This interface provides access to the column values.
 *
 * Note that columns are numbered from 0.
 */
interface Row {
    fun <T : Any> get(index: Int, type: KType): T?

    fun <T : Any> get(columnLabel: String, type: KType): T?
}

inline fun <reified T : Any> Row.get(index: Int): T? {
    return get(index, typeOf<T>())
}

inline fun <reified T : Any> Row.get(columnLabel: String): T? {
    return get(columnLabel, typeOf<T>())
}

inline fun <reified T : Any> Row.getNotNull(index: Int): T {
    val nullable = get<T>(index, typeOf<T>())
    return checkNotNull(nullable) { "The returning value is null. index=$index" }
}

inline fun <reified T : Any> Row.getNotNull(columnLabel: String): T {
    val nullable = get<T>(columnLabel, typeOf<T>())
    return checkNotNull(nullable) { "The returning value is null. columnLabel=$columnLabel" }
}

fun Row.any(index: Int): Any? {
    return get(index)
}

fun Row.any(columnLabel: String): Any? {
    return get(columnLabel)
}

fun Row.anyNotNull(index: Int): Any {
    return getNotNull(index)
}

fun Row.anyNotNull(columnLabel: String): Any {
    return getNotNull(columnLabel)
}

fun Row.bigDecimal(index: Int): BigDecimal? {
    return get(index)
}

fun Row.bigDecimal(columnLabel: String): BigDecimal? {
    return get(columnLabel)
}

fun Row.bigDecimalNotNull(index: Int): BigDecimal {
    return getNotNull(index)
}

fun Row.bigDecimalNotNull(columnLabel: String): BigDecimal {
    return getNotNull(columnLabel)
}

fun Row.bigInteger(index: Int): BigInteger? {
    return get(index)
}

fun Row.bigInteger(columnLabel: String): BigInteger? {
    return get(columnLabel)
}

fun Row.bigIntegerNotNull(index: Int): BigInteger {
    return getNotNull(index)
}

fun Row.bigIntegerNotNull(columnLabel: String): BigInteger {
    return getNotNull(columnLabel)
}

fun Row.boolean(index: Int): Boolean? {
    return get(index)
}

fun Row.boolean(columnLabel: String): Boolean? {
    return get(columnLabel)
}

fun Row.booleanNotNull(index: Int): Boolean {
    return getNotNull(index)
}

fun Row.booleanNotNull(columnLabel: String): Boolean {
    return getNotNull(columnLabel)
}

fun Row.byte(index: Int): Byte? {
    return get(index)
}

fun Row.byte(columnLabel: String): Byte? {
    return get(columnLabel)
}

fun Row.byteNotNull(index: Int): Byte {
    return getNotNull(index)
}

fun Row.byteNotNull(columnLabel: String): Byte {
    return getNotNull(columnLabel)
}

fun Row.byteArray(index: Int): ByteArray? {
    return get(index)
}

fun Row.byteArray(columnLabel: String): ByteArray? {
    return get(columnLabel)
}

fun Row.byteArrayNotNull(index: Int): ByteArray {
    return getNotNull(index)
}

fun Row.byteArrayNotNull(columnLabel: String): ByteArray {
    return getNotNull(columnLabel)
}

fun Row.double(index: Int): Double? {
    return get(index)
}

fun Row.double(columnLabel: String): Double? {
    return get(columnLabel)
}

fun Row.doubleNotNull(index: Int): Double {
    return getNotNull(index)
}

fun Row.doubleNotNull(columnLabel: String): Double {
    return getNotNull(columnLabel)
}

fun Row.float(index: Int): Float? {
    return get(index)
}

fun Row.float(columnLabel: String): Float? {
    return get(columnLabel)
}

fun Row.floatNotNull(index: Int): Float {
    return getNotNull(index)
}

fun Row.floatNotNull(columnLabel: String): Float {
    return getNotNull(columnLabel)
}

fun Row.int(index: Int): Int? {
    return get(index)
}

fun Row.int(columnLabel: String): Int? {
    return get(columnLabel)
}

fun Row.intNotNull(index: Int): Int {
    return getNotNull(index)
}

fun Row.intNotNull(columnLabel: String): Int {
    return getNotNull(columnLabel)
}

fun Row.localDateTime(index: Int): LocalDateTime? {
    return get(index)
}

fun Row.localDateTime(columnLabel: String): LocalDateTime? {
    return get(columnLabel)
}

fun Row.localDateTimeNotNull(index: Int): LocalDateTime {
    return getNotNull(index)
}

fun Row.localDateTimeNotNull(columnLabel: String): LocalDateTime {
    return getNotNull(columnLabel)
}

fun Row.localDate(index: Int): LocalDate? {
    return get(index)
}

fun Row.localDate(columnLabel: String): LocalDate? {
    return get(columnLabel)
}

fun Row.localDateNotNull(index: Int): LocalDate {
    return getNotNull(index)
}

fun Row.localDateNotNull(columnLabel: String): LocalDate {
    return getNotNull(columnLabel)
}

fun Row.localTime(index: Int): LocalTime? {
    return get(index)
}

fun Row.localTime(columnLabel: String): LocalTime? {
    return get(columnLabel)
}

fun Row.localTimeNotNull(index: Int): LocalTime {
    return getNotNull(index)
}

fun Row.localTimeNotNull(columnLabel: String): LocalTime {
    return getNotNull(columnLabel)
}

fun Row.long(index: Int): Long? {
    return get(index)
}

fun Row.long(columnLabel: String): Long? {
    return get(columnLabel)
}

fun Row.longNotNull(index: Int): Long {
    return getNotNull(index)
}

fun Row.longNotNull(columnLabel: String): Long {
    return getNotNull(columnLabel)
}

fun Row.offsetDateTime(index: Int): OffsetDateTime? {
    return get(index)
}

fun Row.offsetDateTime(columnLabel: String): OffsetDateTime? {
    return get(columnLabel)
}

fun Row.offsetDateTimeNotNull(index: Int): OffsetDateTime {
    return getNotNull(index)
}

fun Row.offsetDateTimeNotNull(columnLabel: String): OffsetDateTime {
    return getNotNull(columnLabel)
}

fun Row.short(index: Int): Short? {
    return get(index)
}

fun Row.short(columnLabel: String): Short? {
    return get(columnLabel)
}

fun Row.shortNotNull(index: Int): Short {
    return getNotNull(index)
}

fun Row.shortNotNull(columnLabel: String): Short {
    return getNotNull(columnLabel)
}

fun Row.string(index: Int): String? {
    return get(index)
}

fun Row.string(columnLabel: String): String? {
    return get(columnLabel)
}

fun Row.stringNotNull(index: Int): String {
    return getNotNull(index)
}

fun Row.stringNotNull(columnLabel: String): String {
    return getNotNull(columnLabel)
}

fun Row.uuid(index: Int): UUID? {
    return get(index)
}

fun Row.uuid(columnLabel: String): UUID? {
    return get(columnLabel)
}

fun Row.uuidNotNull(index: Int): UUID {
    return getNotNull(index)
}

fun Row.uuidNotNull(columnLabel: String): UUID {
    return getNotNull(columnLabel)
}
