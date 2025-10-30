package org.komapper.jdbc

import org.komapper.core.DataType
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DataTypeConverter
import org.komapper.core.type.BlobByteArray
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
import java.sql.SQLType
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
interface JdbcDataType<T : Any> : DataType {
    @Suppress("DEPRECATION")
    override val sqlType: SQLType
        get() = jdbcType

    /**
     * The JDBC type defined in the standard library.
     */
    @Deprecated("use sqlType instead")
    val jdbcType: JDBCType
        get() = JDBCType.OTHER

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

    /**
     * Converts the given value to its literal string representation.
     *
     * @param value the value to be converted
     * @return the literal string representation of the value
     */
    fun toLiteral(value: T?): String {
        return toString(value)
    }
}

abstract class AbstractJdbcDataType<T : Any>(
    override val type: KType,
    override val sqlType: SQLType,
    private val toLiteral: (DataType.(T?) -> String)? = null,
) : JdbcDataType<T> {
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

    override fun toLiteral(value: T?): String {
        return toLiteral?.invoke(this, value) ?: toString(value)
    }
}

class JdbcAnyType(override val name: String, toLiteral: (DataType.(Any?) -> String)? = null) :
    AbstractJdbcDataType<Any>(typeOf<Any>(), JDBCType.OTHER, toLiteral) {
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

class JdbcArrayType(override val name: String, toLiteral: (DataType.(Array?) -> String)? = null) :
    AbstractJdbcDataType<Array>(typeOf<Array>(), JDBCType.ARRAY, toLiteral) {
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

class JdbcBigDecimalType(override val name: String, toLiteral: (DataType.(BigDecimal?) -> String)? = null) :
    AbstractJdbcDataType<BigDecimal>(typeOf<BigDecimal>(), JDBCType.DECIMAL, toLiteral) {
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

class JdbcBigIntegerType(override val name: String, toLiteral: (DataType.(BigInteger?) -> String)? = null) :
    JdbcDataType<BigInteger> {
    private val dataType = JdbcBigDecimalType(name) {
        val v = it?.toBigInteger()
        if (toLiteral != null) {
            toLiteral(v)
        } else {
            this@JdbcBigIntegerType.toLiteral(v)
        }
    }

    override val type = typeOf<BigInteger>()
    override val sqlType = dataType.sqlType

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

class JdbcBlobType(override val name: String, toLiteral: (DataType.(Blob?) -> String)? = null) :
    AbstractJdbcDataType<Blob>(typeOf<Blob>(), JDBCType.BLOB, toLiteral) {
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

class JdbcBlobByteArrayType(override val name: String, toLiteral: (DataType.(BlobByteArray?) -> String)? = null) :
    AbstractJdbcDataType<BlobByteArray>(typeOf<BlobByteArray>(), JDBCType.BLOB, toLiteral) {
    override fun doGetValue(rs: ResultSet, index: Int): BlobByteArray? {
        return rs.getBlob(index)?.toBlobByteArray()
    }
    override fun doGetValue(rs: ResultSet, columnLabel: String): BlobByteArray? {
        return rs.getBlob(columnLabel)?.toBlobByteArray()
    }
    override fun doSetValue(ps: PreparedStatement, index: Int, value: BlobByteArray) {
        ps.setBytes(index, value.value)
    }

    private fun Blob?.toBlobByteArray(): BlobByteArray? {
        return try {
            this?.binaryStream?.use { input ->
                val output = mutableListOf<Byte>()
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    for (i in 0 until bytesRead) {
                        output.add(buffer[i])
                    }
                }
                BlobByteArray(output.toByteArray())
            }
        } finally {
            runCatching { this?.free() }
        }
    }
}

class JdbcBooleanType(override val name: String, toLiteral: (DataType.(Boolean?) -> String)? = null) :
    AbstractJdbcDataType<Boolean>(typeOf<Boolean>(), JDBCType.BOOLEAN, toLiteral) {
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

class JdbcByteType(override val name: String, toLiteral: (DataType.(Byte?) -> String)? = null) :
    AbstractJdbcDataType<Byte>(typeOf<Byte>(), JDBCType.SMALLINT, toLiteral) {
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

class JdbcByteArrayType(override val name: String, toLiteral: (DataType.(ByteArray?) -> String)? = null) :
    AbstractJdbcDataType<ByteArray>(typeOf<ByteArray>(), JDBCType.BINARY, toLiteral) {
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

class JdbcClobType(override val name: String, toLiteral: (DataType.(Clob?) -> String)? = null) :
    AbstractJdbcDataType<Clob>(typeOf<Clob>(), JDBCType.CLOB, toLiteral) {
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

class JdbcClobStringType(override val name: String, toLiteral: (DataType.(ClobString?) -> String)? = null) :
    AbstractJdbcDataType<ClobString>(typeOf<ClobString>(), JDBCType.CLOB, toLiteral) {
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

class JdbcDoubleType(override val name: String, toLiteral: (DataType.(Double?) -> String)? = null) :
    AbstractJdbcDataType<Double>(typeOf<Double>(), JDBCType.DOUBLE, toLiteral) {
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

class JdbcFloatType(override val name: String, toLiteral: (DataType.(Float?) -> String)? = null) :
    AbstractJdbcDataType<Float>(typeOf<Float>(), JDBCType.FLOAT, toLiteral) {
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

class JdbcInstantType(override val name: String, toLiteral: (DataType.(Instant?) -> String)? = null) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE, toLiteral) {
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

class JdbcInstantAsTimestampType(override val name: String, toLiteral: (DataType.(Instant?) -> String)? = null) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP, toLiteral) {
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

class JdbcInstantAsTimestampWithTimezoneType(
    override val name: String,
    toLiteral: (DataType.(Instant?) -> String)? = null,
) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE, toLiteral) {
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

class JdbcIntType(override val name: String, toLiteral: (DataType.(Int?) -> String)? = null) :
    AbstractJdbcDataType<Int>(typeOf<Int>(), JDBCType.INTEGER, toLiteral) {
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
class JdbcKotlinInstantType(override val name: String, toLiteral: (DataType.(kotlin.time.Instant?) -> String)? = null) :
    AbstractJdbcDataType<kotlin.time.Instant>(
        typeOf<kotlin.time.Instant>(),
        JDBCType.TIMESTAMP_WITH_TIMEZONE,
        toLiteral
    ) {
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
class JdbcKotlinInstantAsTimestampType(
    override val name: String,
    toLiteral: (DataType.(kotlin.time.Instant?) -> String)? = null,
) :
    AbstractJdbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP, toLiteral) {
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
class JdbcKotlinInstantAsTimestampWithTimezoneType(
    override val name: String,
    toLiteral: (DataType.(kotlin.time.Instant?) -> String)? = null,
) :
    AbstractJdbcDataType<kotlin.time.Instant>(
            typeOf<kotlin.time.Instant>(),
            JDBCType.TIMESTAMP_WITH_TIMEZONE,
            toLiteral
        ) {
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

class JdbcLocalDateTimeType(override val name: String, toLiteral: (DataType.(LocalDateTime?) -> String)? = null) :
    AbstractJdbcDataType<LocalDateTime>(typeOf<LocalDateTime>(), JDBCType.TIMESTAMP, toLiteral) {
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

class JdbcLocalDateType(override val name: String, toLiteral: (DataType.(LocalDate?) -> String)? = null) :
    AbstractJdbcDataType<LocalDate>(typeOf<LocalDate>(), JDBCType.DATE, toLiteral) {
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

class JdbcLocalTimeType(override val name: String, toLiteral: (DataType.(LocalTime?) -> String)? = null) :
    AbstractJdbcDataType<LocalTime>(typeOf<LocalTime>(), JDBCType.TIME, toLiteral) {
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

class JdbcLongType(override val name: String, toLiteral: (DataType.(Long?) -> String)? = null) :
    AbstractJdbcDataType<Long>(typeOf<Long>(), JDBCType.BIGINT, toLiteral) {
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

class JdbcNClobType(override val name: String, toLiteral: (DataType.(NClob?) -> String)? = null) :
    AbstractJdbcDataType<NClob>(typeOf<NClob>(), JDBCType.NCLOB, toLiteral) {
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

class JdbcOffsetDateTimeType(override val name: String, toLiteral: (DataType.(OffsetDateTime?) -> String)? = null) :
    AbstractJdbcDataType<OffsetDateTime>(typeOf<OffsetDateTime>(), JDBCType.TIMESTAMP_WITH_TIMEZONE, toLiteral) {
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

class JdbcShortType(override val name: String, toLiteral: (DataType.(Short?) -> String)? = null) :
    AbstractJdbcDataType<Short>(typeOf<Short>(), JDBCType.SMALLINT, toLiteral) {
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

class JdbcStringType(override val name: String, toLiteral: (DataType.(String?) -> String)? = null) :
    AbstractJdbcDataType<String>(typeOf<String>(), JDBCType.VARCHAR, toLiteral) {
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

class JdbcSQLXMLType(override val name: String, toLiteral: (DataType.(SQLXML?) -> String)? = null) :
    AbstractJdbcDataType<SQLXML>(typeOf<SQLXML>(), JDBCType.SQLXML, toLiteral) {
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

class JdbcUByteType(override val name: String, toLiteral: (DataType.(UByte?) -> String)? = null) :
    AbstractJdbcDataType<UByte>(typeOf<UByte>(), JDBCType.SMALLINT, toLiteral) {
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

class JdbcUIntType(override val name: String, toLiteral: (DataType.(UInt?) -> String)? = null) :
    AbstractJdbcDataType<UInt>(typeOf<UInt>(), JDBCType.BIGINT, toLiteral) {
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

class JdbcUShortType(override val name: String, toLiteral: (DataType.(UShort?) -> String)? = null) :
    AbstractJdbcDataType<UShort>(typeOf<UShort>(), JDBCType.INTEGER, toLiteral) {
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
    override val name: String
        get() = dataType.name

    override val type: KType
        get() = dataType.type

    override val sqlType: SQLType
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

    override fun toLiteral(value: T?): String {
        return dataType.toLiteral(value)
    }
}

class JdbcDataTypeProxy<EXTERIOR : Any, INTERIOR : Any>(
    private val converter: DataTypeConverter<EXTERIOR, INTERIOR>,
    private val dataType: JdbcDataType<INTERIOR>,
) : JdbcDataType<EXTERIOR> {
    override val name: String get() = dataType.name

    override val type: KType get() = converter.exteriorType

    override val sqlType: SQLType get() = dataType.sqlType

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

    override fun toLiteral(value: EXTERIOR?): String {
        val v = if (value == null) null else converter.unwrap(value)
        return dataType.toLiteral(v)
    }
}
