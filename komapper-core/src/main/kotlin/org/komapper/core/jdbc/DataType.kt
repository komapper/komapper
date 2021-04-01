package org.komapper.core.jdbc

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLXML
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

interface DataType<T> {
    fun getValue(rs: ResultSet, index: Int): T?
    fun getValue(rs: ResultSet, columnLabel: String): T?
    fun setValue(ps: PreparedStatement, index: Int, value: T?)
    fun toString(value: T?): String
}

abstract class AbstractDataType<T>(protected val sqlType: Int) : DataType<T> {

    override fun getValue(rs: ResultSet, index: Int): T? {
        val value = doGetValue(rs, index)
        return if (rs.wasNull()) null else value
    }

    override fun getValue(rs: ResultSet, columnLabel: String): T? {
        val value = doGetValue(rs, columnLabel)
        return if (rs.wasNull()) null else value
    }

    protected abstract fun doGetValue(rs: ResultSet, index: Int): T?

    protected abstract fun doGetValue(rs: ResultSet, columnLabel: String): T?

    override fun setValue(ps: PreparedStatement, index: Int, value: T?) {
        if (value == null) {
            ps.setNull(index, sqlType)
        } else {
            doSetValue(ps, index, value)
        }
    }

    protected abstract fun doSetValue(ps: PreparedStatement, index: Int, value: T)

    override fun toString(value: T?): String {
        return if (value == null) "null" else doToString(value)
    }

    open fun doToString(value: T): String {
        return value.toString()
    }
}

object AnyType : AbstractDataType<Any>(Types.OTHER) {

    override fun doGetValue(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Any? {
        return rs.getObject(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Any) {
        ps.setObject(index, value, sqlType)
    }
}

object ArrayType : AbstractDataType<Array>(Types.ARRAY) {

    override fun doGetValue(rs: ResultSet, index: Int): Array? {
        return rs.getArray(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Array? {
        return rs.getArray(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Array) {
        ps.setArray(index, value)
    }
}

object BigDecimalType : AbstractDataType<BigDecimal>(Types.DECIMAL) {

    override fun doGetValue(rs: ResultSet, index: Int): BigDecimal? {
        return rs.getBigDecimal(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): BigDecimal? {
        return rs.getBigDecimal(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: BigDecimal) {
        ps.setBigDecimal(index, value)
    }
}

object BigIntegerType : DataType<BigInteger> {

    private val jdbcType = BigDecimalType

    override fun getValue(rs: ResultSet, index: Int): BigInteger? {
        return jdbcType.getValue(rs, index)?.toBigInteger()
    }

    override fun getValue(rs: ResultSet, columnLabel: String): BigInteger? {
        return jdbcType.getValue(rs, columnLabel)?.toBigInteger()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: BigInteger?) {
        jdbcType.setValue(ps, index, value?.toBigDecimal())
    }

    override fun toString(value: BigInteger?): String {
        return jdbcType.toString(value?.toBigDecimal())
    }
}

object BlobType : AbstractDataType<Blob>(Types.BLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): Blob? {
        return rs.getBlob(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Blob? {
        return rs.getBlob(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Blob) {
        ps.setBlob(index, value)
    }
}

object BooleanType : AbstractDataType<Boolean>(Types.BOOLEAN) {

    override fun doGetValue(rs: ResultSet, index: Int): Boolean {
        return rs.getBoolean(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Boolean {
        return rs.getBoolean(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Boolean) {
        ps.setBoolean(index, value)
    }

    override fun doToString(value: Boolean): String {
        return "'$value'"
    }
}

object ByteType : AbstractDataType<Byte>(Types.SMALLINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Byte {
        return rs.getByte(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Byte {
        return rs.getByte(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Byte) {
        ps.setByte(index, value)
    }
}

object ByteArrayType : AbstractDataType<ByteArray>(Types.BINARY) {

    override fun doGetValue(rs: ResultSet, index: Int): ByteArray? {
        return rs.getBytes(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): ByteArray? {
        return rs.getBytes(columnLabel)
    }
    override fun doSetValue(ps: PreparedStatement, index: Int, value: ByteArray) {
        ps.setBytes(index, value)
    }
}

object ClobType : AbstractDataType<Clob>(Types.CLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): Clob? {
        return rs.getClob(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Clob? {
        return rs.getClob(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Clob) {
        ps.setClob(index, value)
    }
}

object DoubleType : AbstractDataType<Double>(Types.DOUBLE) {

    override fun doGetValue(rs: ResultSet, index: Int): Double {
        return rs.getDouble(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Double {
        return rs.getDouble(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Double) {
        ps.setDouble(index, value)
    }
}

class EnumType(private val kClass: KClass<Enum<*>>) : DataType<Enum<*>> {

    private val jdbcType = StringType

    override fun getValue(rs: ResultSet, index: Int): Enum<*>? {
        val value = jdbcType.getValue(rs, index) ?: return null
        return toEnumConstant(value)
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Enum<*>? {
        val value = jdbcType.getValue(rs, columnLabel) ?: return null
        return toEnumConstant(value)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Enum<*>?) {
        jdbcType.setValue(ps, index, value?.name)
    }

    override fun toString(value: Enum<*>?): String {
        return jdbcType.toString(value?.name)
    }

    fun toEnumConstant(value: String): Enum<*> {
        return kClass.java.enumConstants.first { it.name == value }
    }
}

object FloatType : AbstractDataType<Float>(Types.FLOAT) {

    override fun doGetValue(rs: ResultSet, index: Int): Float {
        return rs.getFloat(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Float {
        return rs.getFloat(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Float) {
        ps.setFloat(index, value)
    }
}

object IntType : AbstractDataType<Int>(Types.INTEGER) {

    override fun doGetValue(rs: ResultSet, index: Int): Int {
        return rs.getInt(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Int {
        return rs.getInt(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Int) {
        ps.setInt(index, value)
    }
}

object LocalDateTimeType : AbstractDataType<LocalDateTime>(Types.TIMESTAMP) {

    override fun doGetValue(rs: ResultSet, index: Int): LocalDateTime? {
        return rs.getObject(index, LocalDateTime::class.java)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): LocalDateTime? {
        return rs.getObject(columnLabel, LocalDateTime::class.java)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: LocalDateTime) {
        ps.setObject(index, value)
    }

    override fun doToString(value: LocalDateTime): String {
        return "'$value'"
    }
}

object LocalDateType : AbstractDataType<LocalDate>(Types.DATE) {

    override fun doGetValue(rs: ResultSet, index: Int): LocalDate? {
        return rs.getObject(index, LocalDate::class.java)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): LocalDate? {
        return rs.getObject(columnLabel, LocalDate::class.java)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: LocalDate) {
        ps.setObject(index, value)
    }

    override fun doToString(value: LocalDate): String {
        return "'$value'"
    }
}

object LocalTimeType : AbstractDataType<LocalTime>(Types.TIME) {

    override fun doGetValue(rs: ResultSet, index: Int): LocalTime? {
        return rs.getObject(index, LocalTime::class.java)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): LocalTime? {
        return rs.getObject(columnLabel, LocalTime::class.java)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: LocalTime) {
        ps.setObject(index, value)
    }

    override fun doToString(value: LocalTime): String {
        return "'$value'"
    }
}

object LongType : AbstractDataType<Long>(Types.BIGINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Long {
        return rs.getLong(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Long {
        return rs.getLong(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Long) {
        ps.setLong(index, value)
    }
}

object NClobType : AbstractDataType<NClob>(Types.NCLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): NClob? {
        return rs.getNClob(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): NClob? {
        return rs.getNClob(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: NClob) {
        ps.setNClob(index, value)
    }
}

object OffsetDateTimeType : AbstractDataType<OffsetDateTime>(Types.TIMESTAMP_WITH_TIMEZONE) {

    override fun doGetValue(rs: ResultSet, index: Int): OffsetDateTime? {
        return rs.getObject(index, OffsetDateTime::class.java)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): OffsetDateTime? {
        return rs.getObject(columnLabel, OffsetDateTime::class.java)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: OffsetDateTime) {
        ps.setObject(index, value)
    }

    override fun doToString(value: OffsetDateTime): String {
        return "'$value'"
    }
}

object ShortType : AbstractDataType<Short>(Types.SMALLINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Short {
        return rs.getShort(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Short {
        return rs.getShort(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Short) {
        ps.setShort(index, value)
    }
}

object StringType : AbstractDataType<String>(Types.VARCHAR) {

    override fun doGetValue(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): String? {
        return rs.getString(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: String) {
        ps.setString(index, value)
    }

    override fun doToString(value: String): String {
        return "'$value'"
    }
}

object SQLXMLType : AbstractDataType<SQLXML>(Types.SQLXML) {

    override fun doGetValue(rs: ResultSet, index: Int): SQLXML? {
        return rs.getSQLXML(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): SQLXML? {
        return rs.getSQLXML(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: SQLXML) {
        ps.setSQLXML(index, value)
    }
}
