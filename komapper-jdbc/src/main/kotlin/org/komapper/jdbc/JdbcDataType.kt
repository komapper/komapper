package org.komapper.jdbc

import org.komapper.core.SqlType
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DataTypeConverter
import org.komapper.core.type.ClobString
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.JDBCType
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLXML
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * Represents a data type for JDBC access.
 */
@ThreadSafe
interface JdbcDataType<T : Any> : SqlType {
    /**
     * The JDBC type defined in the standard library.
     */
    val jdbcType: JDBCType
        get() = sqlType

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param index the index
     * @return the value
     */
    fun getValue(rs: ResultSet, index: Int): T?

    /**
     * Returns the value.
     * @param rs the result set
     * @param columnLabel the column label
     * @return the value
     */
    fun getValue(rs: ResultSet, columnLabel: String): T?

    /**
     * Sets the value.
     *
     * @param ps the prepared statement
     * @param index the index
     */
    fun setValue(ps: PreparedStatement, index: Int, value: T?)

    /**
     * Registers the return parameter.
     *
     * @param ps the prepared statement
     * @param index the index
     */
    fun registerReturnParameter(ps: PreparedStatement, index: Int) {
        throw UnsupportedOperationException()
    }

    /**
     * Returns the string presentation of the value.
     *
     * @param value the value
     * @return the string presentation of the value
     */
    fun toString(value: T?): String
}

abstract class AbstractJdbcDataType<T : Any>(
    override val type: KType,
    override val sqlType: JDBCType,
) : JdbcDataType<T> {
    override val jdbcType: JDBCType
        get() = sqlType

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
            ps.setNull(index, sqlType.vendorTypeNumber)
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

class JdbcAnyType(override val name: String) : AbstractJdbcDataType<Any>(typeOf<Any>(), JDBCType.OTHER) {
    override fun doGetValue(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Any? {
        return rs.getObject(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Any) {
        ps.setObject(index, value, sqlType.vendorTypeNumber)
    }
}

class JdbcArrayType(override val name: String) : AbstractJdbcDataType<Array>(typeOf<Array>(), JDBCType.ARRAY) {
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

class JdbcBigDecimalType(override val name: String) :
    AbstractJdbcDataType<BigDecimal>(typeOf<BigDecimal>(), JDBCType.DECIMAL) {
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

class JdbcBigIntegerType(override val name: String) : JdbcDataType<BigInteger> {
    private val dataType = JdbcBigDecimalType(name)

    override val type = typeOf<BigInteger>()
    override val sqlType = dataType.sqlType

    override val jdbcType: JDBCType
        get() = sqlType

    override fun getValue(rs: ResultSet, index: Int): BigInteger? {
        return dataType.getValue(rs, index)?.toBigInteger()
    }

    override fun getValue(rs: ResultSet, columnLabel: String): BigInteger? {
        return dataType.getValue(rs, columnLabel)?.toBigInteger()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: BigInteger?) {
        dataType.setValue(ps, index, value?.toBigDecimal())
    }

    override fun toString(value: BigInteger?): String {
        return dataType.toString(value?.toBigDecimal())
    }
}

class JdbcBlobType(override val name: String) :
    AbstractJdbcDataType<Blob>(typeOf<Blob>(), JDBCType.BLOB) {
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

class JdbcBooleanType(override val name: String) :
    AbstractJdbcDataType<Boolean>(typeOf<Boolean>(), JDBCType.BOOLEAN) {
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
        return value.toString().uppercase()
    }
}

class JdbcByteType(override val name: String) : AbstractJdbcDataType<Byte>(typeOf<Byte>(), JDBCType.SMALLINT) {
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

class JdbcByteArrayType(override val name: String) :
    AbstractJdbcDataType<ByteArray>(typeOf<ByteArray>(), JDBCType.BINARY) {
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

class JdbcClobType(override val name: String) : AbstractJdbcDataType<Clob>(typeOf<Clob>(), JDBCType.CLOB) {
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

class JdbcClobStringType(override val name: String) : AbstractJdbcDataType<ClobString>(typeOf<ClobString>(), JDBCType.CLOB) {
    override fun doGetValue(rs: ResultSet, index: Int): ClobString? {
        val text = rs.getString(index)
        return if (text == null) null else ClobString(text)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): ClobString? {
        val text = rs.getString(columnLabel)
        return if (text == null) null else ClobString(text)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: ClobString) {
        ps.setString(index, value.value)
    }

    override fun doToString(value: ClobString): String {
        return value.value
    }
}

class JdbcDoubleType(override val name: String) :
    AbstractJdbcDataType<Double>(typeOf<Double>(), JDBCType.DOUBLE) {
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

class JdbcFloatType(override val name: String) : AbstractJdbcDataType<Float>(typeOf<Float>(), JDBCType.FLOAT) {
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

class JdbcInstantType(override val name: String) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    override fun doGetValue(rs: ResultSet, index: Int): Instant? {
        return rs.getObject(index, Instant::class.java)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Instant? {
        return rs.getObject(columnLabel, Instant::class.java)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Instant) {
        ps.setObject(index, value)
    }
}

class JdbcInstantAsTimestampType(override val name: String) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP) {
    override fun doGetValue(rs: ResultSet, index: Int): Instant? {
        val dateTime: LocalDateTime? = rs.getObject(index, LocalDateTime::class.java)
        return dateTime?.let { toInstant(it) }
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Instant? {
        val dateTime: LocalDateTime? = rs.getObject(columnLabel, LocalDateTime::class.java)
        return dateTime?.let { toInstant(it) }
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Instant) {
        val datetime = LocalDateTime.ofInstant(value, ZoneOffset.UTC)
        ps.setObject(index, datetime)
    }

    private fun toInstant(dateTime: LocalDateTime): Instant {
        return dateTime.toInstant(ZoneOffset.UTC)
    }
}

class JdbcInstantAsTimestampWithTimezoneType(override val name: String) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    override fun doGetValue(rs: ResultSet, index: Int): Instant? {
        val dateTime: OffsetDateTime? = rs.getObject(index, OffsetDateTime::class.java)
        return dateTime?.toInstant()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Instant? {
        val dateTime: OffsetDateTime? = rs.getObject(columnLabel, OffsetDateTime::class.java)
        return dateTime?.toInstant()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Instant) {
        val dateTime = value.atOffset(ZoneOffset.UTC)
        ps.setObject(index, dateTime)
    }
}

class JdbcIntType(override val name: String) : AbstractJdbcDataType<Int>(typeOf<Int>(), JDBCType.INTEGER) {
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

@OptIn(ExperimentalTime::class)
class JdbcKotlinInstantType(override val name: String) :
    AbstractJdbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    val instantType = JdbcInstantType(name)

    override fun doGetValue(rs: ResultSet, index: Int): kotlin.time.Instant? {
        return instantType.getValue(rs, index)?.toKotlinInstant()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): kotlin.time.Instant? {
        return instantType.getValue(rs, columnLabel)?.toKotlinInstant()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: kotlin.time.Instant) {
        instantType.setValue(ps, index, value.toJavaInstant())
    }
}

@OptIn(ExperimentalTime::class)
class JdbcKotlinInstantAsTimestampType(override val name: String) :
    AbstractJdbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP) {
    private val instantAsTimestampType = JdbcInstantAsTimestampType(name)

    override fun doGetValue(rs: ResultSet, index: Int): kotlin.time.Instant? {
        return instantAsTimestampType.getValue(rs, index)?.toKotlinInstant()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): kotlin.time.Instant? {
        return instantAsTimestampType.getValue(rs, columnLabel)?.toKotlinInstant()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: kotlin.time.Instant) {
        instantAsTimestampType.setValue(ps, index, value.toJavaInstant())
    }
}

@OptIn(ExperimentalTime::class)
class JdbcKotlinInstantAsTimestampWithTimezoneType(override val name: String) :
    AbstractJdbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    private val instantAsTimestampWithTimezoneType = JdbcInstantAsTimestampWithTimezoneType(name)

    override fun doGetValue(rs: ResultSet, index: Int): kotlin.time.Instant? {
        return instantAsTimestampWithTimezoneType.getValue(rs, index)?.toKotlinInstant()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): kotlin.time.Instant? {
        return instantAsTimestampWithTimezoneType.getValue(rs, columnLabel)?.toKotlinInstant()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: kotlin.time.Instant) {
        instantAsTimestampWithTimezoneType.setValue(ps, index, value.toJavaInstant())
    }
}

class JdbcLocalDateTimeType(override val name: String) :
    AbstractJdbcDataType<LocalDateTime>(typeOf<LocalDateTime>(), JDBCType.TIMESTAMP) {
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

class JdbcLocalDateType(override val name: String) :
    AbstractJdbcDataType<LocalDate>(typeOf<LocalDate>(), JDBCType.DATE) {
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

class JdbcLocalTimeType(override val name: String) :
    AbstractJdbcDataType<LocalTime>(typeOf<LocalTime>(), JDBCType.TIME) {
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

class JdbcLongType(override val name: String) : AbstractJdbcDataType<Long>(typeOf<Long>(), JDBCType.BIGINT) {
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

class JdbcNClobType(override val name: String) : AbstractJdbcDataType<NClob>(typeOf<NClob>(), JDBCType.NCLOB) {
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

class JdbcOffsetDateTimeType(override val name: String) :
    AbstractJdbcDataType<OffsetDateTime>(typeOf<OffsetDateTime>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
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

class JdbcShortType(override val name: String) :
    AbstractJdbcDataType<Short>(typeOf<Short>(), JDBCType.SMALLINT) {
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

class JdbcStringType(override val name: String) :
    AbstractJdbcDataType<String>(typeOf<String>(), JDBCType.VARCHAR) {
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

class JdbcSQLXMLType(override val name: String) :
    AbstractJdbcDataType<SQLXML>(typeOf<SQLXML>(), JDBCType.SQLXML) {
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

class JdbcUByteType(override val name: String) : AbstractJdbcDataType<UByte>(typeOf<UByte>(), JDBCType.SMALLINT) {
    override fun doGetValue(rs: ResultSet, index: Int): UByte {
        val value = rs.getShort(index)
        if (value < 0) error("Negative value isn't convertible to UByte. index=$index, value=$value")
        return value.toUByte()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UByte {
        val value = rs.getShort(columnLabel)
        if (value < 0) error("Negative value isn't convertible to UByte. columnLabel=$columnLabel, value=$value")
        return value.toUByte()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UByte) {
        ps.setShort(index, value.toShort())
    }
}

class JdbcUIntType(override val name: String) : AbstractJdbcDataType<UInt>(typeOf<UInt>(), JDBCType.BIGINT) {
    override fun doGetValue(rs: ResultSet, index: Int): UInt {
        val value = rs.getLong(index)
        if (value < 0L) error("Negative value isn't convertible to UInt. index=$index, value=$value")
        return value.toUInt()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UInt {
        val value = rs.getLong(columnLabel)
        if (value < 0L) error("Negative value isn't convertible to UInt. columnLabel=$columnLabel, value=$value")
        return value.toUInt()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UInt) {
        ps.setLong(index, value.toLong())
    }
}

class JdbcUShortType(override val name: String) : AbstractJdbcDataType<UShort>(typeOf<UShort>(), JDBCType.INTEGER) {
    override fun doGetValue(rs: ResultSet, index: Int): UShort {
        val value = rs.getInt(index)
        if (value < 0L) error("Negative value isn't convertible to UShort. index=$index, value=$value")
        return value.toUShort()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UShort {
        val value = rs.getInt(columnLabel)
        if (value < 0L) error("Negative value isn't convertible to UShort. columnLabel=$columnLabel, value=$value")
        return value.toUShort()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UShort) {
        ps.setInt(index, value.toInt())
    }
}

class JdbcUserDefinedDataTypeAdapter<T : Any>(
    private val dataType: JdbcUserDefinedDataType<T>,
) : JdbcDataType<T> {
    override val jdbcType: JDBCType
        get() = sqlType

    override val name: String
        get() = dataType.name

    override val type: KType
        get() = dataType.type

    override val sqlType: JDBCType
        get() = dataType.sqlType

    override fun getValue(rs: ResultSet, index: Int): T? {
        val value = dataType.getValue(rs, index)
        return if (rs.wasNull()) null else value
    }

    override fun getValue(rs: ResultSet, columnLabel: String): T? {
        val value = dataType.getValue(rs, columnLabel)
        return if (rs.wasNull()) null else value
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: T?) {
        if (value == null) {
            ps.setNull(index, dataType.sqlType.vendorTypeNumber)
        } else {
            dataType.setValue(ps, index, value)
        }
    }

    override fun toString(value: T?): String {
        return if (value == null) "null" else dataType.toString(value)
    }
}

class JdbcDataTypeProxy<EXTERIOR : Any, INTERIOR : Any>(
    private val converter: DataTypeConverter<EXTERIOR, INTERIOR>,
    private val dataType: JdbcDataType<INTERIOR>,
) : JdbcDataType<EXTERIOR> {
    override val jdbcType: JDBCType
        get() = sqlType

    override val name: String get() = dataType.name

    override val type: KType get() = converter.exteriorType

    override val sqlType: JDBCType get() = dataType.sqlType

    override fun getValue(rs: ResultSet, index: Int): EXTERIOR? {
        val value = dataType.getValue(rs, index)
        return if (rs.wasNull() || value == null) null else converter.wrap(value)
    }

    override fun getValue(rs: ResultSet, columnLabel: String): EXTERIOR? {
        val value = dataType.getValue(rs, columnLabel)
        return if (rs.wasNull() || value == null) null else converter.wrap(value)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: EXTERIOR?) {
        if (value == null) {
            ps.setNull(index, dataType.sqlType.vendorTypeNumber)
        } else {
            dataType.setValue(ps, index, converter.unwrap(value))
        }
    }

    override fun toString(value: EXTERIOR?): String {
        return if (value == null) "null" else dataType.toString(converter.unwrap(value))
    }
}
