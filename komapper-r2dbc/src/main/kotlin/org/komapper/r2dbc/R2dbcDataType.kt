package org.komapper.r2dbc

import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.runBlocking
import org.komapper.core.DataType
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DataTypeConverter
import org.komapper.core.type.BlobByteArray
import org.komapper.core.type.ClobString
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.sql.JDBCType
import java.sql.SQLType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * Represents a data type for R2DBC access.
 */
@ThreadSafe
interface R2dbcDataType<T : Any> : DataType {
    /**
     * Returns the value.
     *
     * @param row the row
     * @param index the index
     * @return the value
     */
    fun getValue(row: Row, index: Int): T?

    /**
     * Returns the value.
     *
     * @param row the row
     * @param columnLabel the column label
     * @return the value
     */
    fun getValue(row: Row, columnLabel: String): T?

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param index the index
     * @param value the value
     */
    fun setValue(statement: Statement, index: Int, value: T?)

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param name the name of identifier to bind to
     * @param value the value
     */
    fun setValue(statement: Statement, name: String, value: T?)

    /**
     * Returns the string presentation of the value.
     *
     * @param value the value
     * @return the string presentation of the value
     */
    fun toString(value: T?): String
}

abstract class AbstractR2dbcDataType<T : Any>(
    override val type: KType,
    override val sqlType: SQLType = JDBCType.OTHER,
    val typeOfNull: Class<*> = (type.classifier as KClass<*>).javaObjectType,
) : R2dbcDataType<T> {
    override fun getValue(row: Row, index: Int): T? {
        return row[index]?.let { convertBeforeGetting(it) }
    }

    override fun getValue(row: Row, columnLabel: String): T? {
        return row[columnLabel]?.let { convertBeforeGetting(it) }
    }

    protected open fun convertBeforeGetting(value: Any): T {
        throw UnsupportedOperationException()
    }

    override fun setValue(statement: Statement, index: Int, value: T?) {
        if (value == null) {
            statement.bindNull(index, typeOfNull)
        } else {
            bind(statement, index, value)
        }
    }

    override fun setValue(statement: Statement, name: String, value: T?) {
        if (value == null) {
            statement.bindNull(name, typeOfNull)
        } else {
            bind(statement, name, value)
        }
    }

    protected open fun bind(statement: Statement, index: Int, value: T) {
        statement.bind(index, convertBeforeBinding(value))
    }

    protected open fun bind(statement: Statement, name: String, value: T) {
        statement.bind(name, convertBeforeBinding(value))
    }

    protected open fun convertBeforeBinding(value: T): Any {
        return value
    }

    override fun toString(value: T?): String {
        return if (value == null) "null" else doToString(value)
    }

    protected open fun doToString(value: T): String {
        return value.toString()
    }
}

class R2dbcBigDecimalType(override val name: String) :
    AbstractR2dbcDataType<BigDecimal>(typeOf<BigDecimal>(), JDBCType.DECIMAL) {
    override fun convertBeforeGetting(value: Any): BigDecimal {
        return when (value) {
            is Double -> value.toBigDecimal()
            is Float -> value.toBigDecimal()
            is Int -> value.toBigDecimal()
            is Long -> value.toBigDecimal()
            is BigDecimal -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcBigIntegerType(override val name: String) : R2dbcDataType<BigInteger> {
    private val dataType = R2dbcBigDecimalType(name)

    override val type = typeOf<BigInteger>()
    override val sqlType = dataType.sqlType

    override fun getValue(row: Row, index: Int): BigInteger? {
        return dataType.getValue(row, index)?.toBigInteger()
    }

    override fun getValue(row: Row, columnLabel: String): BigInteger? {
        return dataType.getValue(row, columnLabel)?.toBigInteger()
    }

    override fun setValue(statement: Statement, index: Int, value: BigInteger?) {
        dataType.setValue(statement, index, value?.toBigDecimal())
    }

    override fun setValue(statement: Statement, name: String, value: BigInteger?) {
        dataType.setValue(statement, name, value?.toBigDecimal())
    }

    override fun toString(value: BigInteger?): String {
        return dataType.toString(value?.toBigDecimal())
    }
}

class R2dbcBlobType(override val name: String) :
    AbstractR2dbcDataType<Blob>(typeOf<Blob>(), JDBCType.BLOB) {
    override fun getValue(row: Row, index: Int): Blob? {
        return row.get(index, Blob::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Blob? {
        return row.get(columnLabel, Blob::class.java)
    }
}

class R2dbcBlobByteArrayType(override val name: String) :
    AbstractR2dbcDataType<BlobByteArray>(typeOf<BlobByteArray>(), JDBCType.BLOB, ByteArray::class.javaObjectType) {
    override fun convertBeforeGetting(value: Any): BlobByteArray {
        return when (value) {
            is Blob -> runBlocking(Dispatchers.IO) { BlobByteArray(value.toByteArray()) }
            is ByteArray -> BlobByteArray(value)
            is ByteBuffer -> BlobByteArray(value.array())
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
    override fun convertBeforeBinding(value: BlobByteArray): Any {
        return value.value
    }

    private suspend fun Blob.toByteArray(): ByteArray {
        return try {
            val output = mutableListOf<Byte>()
            this.stream().collect { buffer ->
                for (byte in buffer.array()) {
                    output.add(byte)
                }
            }
            output.toByteArray()
        } finally {
            runCatching { discard() }
        }
    }
}

class R2dbcBooleanType(override val name: String) :
    AbstractR2dbcDataType<Boolean>(typeOf<Boolean>(), JDBCType.BOOLEAN) {
    override fun convertBeforeGetting(value: Any): Boolean {
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() == 1
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: Boolean): String {
        return value.toString().uppercase()
    }
}

class R2dbcByteType(override val name: String) :
    AbstractR2dbcDataType<Byte>(typeOf<Byte>(), JDBCType.BINARY) {
    override fun convertBeforeGetting(value: Any): Byte {
        return when (value) {
            is Number -> value.toByte()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcByteArrayType(override val name: String) :
    AbstractR2dbcDataType<ByteArray>(typeOf<ByteArray>(), JDBCType.CLOB) {
    override fun convertBeforeGetting(value: Any): ByteArray {
        return when (value) {
            is ByteArray -> value
            is ByteBuffer -> value.array()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcClobType(override val name: String) :
    AbstractR2dbcDataType<Clob>(typeOf<Clob>(), JDBCType.CLOB) {
    override fun getValue(row: Row, index: Int): Clob? {
        return row.get(index, Clob::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Clob? {
        return row.get(columnLabel, Clob::class.java)
    }
}

class R2dbcClobStringType(override val name: String) :
    AbstractR2dbcDataType<ClobString>(typeOf<ClobString>(), JDBCType.CLOB, Clob::class.javaObjectType) {
    override fun getValue(row: Row, index: Int): ClobString? {
        val text = row.get(index, String::class.java)
        return if (text == null) null else ClobString(text)
    }

    override fun getValue(row: Row, columnLabel: String): ClobString? {
        val text = row.get(columnLabel, String::class.java)
        return if (text == null) null else ClobString(text)
    }

    override fun convertBeforeBinding(value: ClobString): Any {
        return value.value
    }

    override fun doToString(value: ClobString): String {
        return value.value
    }
}

class R2dbcDoubleType(override val name: String) :
    AbstractR2dbcDataType<Double>(typeOf<Double>(), JDBCType.DOUBLE) {
    override fun convertBeforeGetting(value: Any): Double {
        return when (value) {
            is Number -> value.toDouble()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcFloatType(override val name: String) :
    AbstractR2dbcDataType<Float>(typeOf<Float>(), JDBCType.FLOAT) {
    override fun convertBeforeGetting(value: Any): Float {
        return when (value) {
            is Number -> value.toFloat()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcInstantType(override val name: String) : AbstractR2dbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    override fun getValue(row: Row, index: Int): Instant? {
        return row.get(index, Instant::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Instant? {
        return row.get(columnLabel, Instant::class.java)
    }
}

class R2dbcInstantAsTimestampType(override val name: String) :
    AbstractR2dbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP, LocalDateTime::class.java) {
    override fun convertBeforeGetting(value: Any): Instant {
        return when (value) {
            is LocalDateTime -> value.toInstant(ZoneOffset.UTC)
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun convertBeforeBinding(value: Instant): Any {
        return LocalDateTime.ofInstant(value, ZoneOffset.UTC)
    }
}

class R2dbcInstantAsTimestampWithTimezoneType(override val name: String) :
    AbstractR2dbcDataType<Instant>(typeOf<Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime::class.java) {
    override fun convertBeforeGetting(value: Any): Instant {
        return when (value) {
            is LocalDateTime -> value.toInstant(ZoneOffset.UTC)
            is OffsetDateTime -> value.toInstant()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun convertBeforeBinding(value: Instant): Any {
        return value.atOffset(ZoneOffset.UTC)
    }
}

class R2dbcIntType(override val name: String) :
    AbstractR2dbcDataType<Int>(typeOf<Int>(), JDBCType.INTEGER) {
    override fun convertBeforeGetting(value: Any): Int {
        return when (value) {
            is Number -> value.toInt()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

@OptIn(ExperimentalTime::class)
class R2dbcKotlinInstantType(override val name: String) : AbstractR2dbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    val instantType = R2dbcInstantType(name)

    override fun getValue(row: Row, index: Int): kotlin.time.Instant? {
        return instantType.getValue(row, index)?.toKotlinInstant()
    }

    override fun getValue(row: Row, columnLabel: String): kotlin.time.Instant? {
        return instantType.getValue(row, columnLabel)?.toKotlinInstant()
    }

    override fun convertBeforeBinding(value: kotlin.time.Instant): Any {
        return value.toJavaInstant()
    }
}

@OptIn(ExperimentalTime::class)
class R2dbcKotlinInstantAsTimestampType(override val name: String) :
    AbstractR2dbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP, LocalDateTime::class.java) {
    private val instantAsTimestampType = R2dbcInstantAsTimestampType(name)

    override fun getValue(row: Row, index: Int): kotlin.time.Instant? {
        return instantAsTimestampType.getValue(row, index)?.toKotlinInstant()
    }

    override fun getValue(row: Row, columnLabel: String): kotlin.time.Instant? {
        return instantAsTimestampType.getValue(row, columnLabel)?.toKotlinInstant()
    }

    override fun convertBeforeBinding(value: kotlin.time.Instant): Any {
        return LocalDateTime.ofInstant(value.toJavaInstant(), ZoneOffset.UTC)
    }
}

@OptIn(ExperimentalTime::class)
class R2dbcKotlinInstantAsTimestampWithTimezoneType(override val name: String) :
    AbstractR2dbcDataType<kotlin.time.Instant>(typeOf<kotlin.time.Instant>(), JDBCType.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime::class.java) {
    private val instantAsTimestampWithTimezoneType = R2dbcInstantAsTimestampWithTimezoneType(name)

    override fun getValue(row: Row, index: Int): kotlin.time.Instant? {
        return instantAsTimestampWithTimezoneType.getValue(row, index)?.toKotlinInstant()
    }

    override fun getValue(row: Row, columnLabel: String): kotlin.time.Instant? {
        return instantAsTimestampWithTimezoneType.getValue(row, columnLabel)?.toKotlinInstant()
    }

    override fun convertBeforeBinding(value: kotlin.time.Instant): Any {
        return value.toJavaInstant().atOffset(ZoneOffset.UTC)
    }
}

class R2dbcLocalDateTimeType(override val name: String) :
    AbstractR2dbcDataType<LocalDateTime>(typeOf<LocalDateTime>(), JDBCType.TIMESTAMP) {
    override fun convertBeforeGetting(value: Any): LocalDateTime {
        return when (value) {
            is LocalDateTime -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: LocalDateTime): String {
        return "'$value'"
    }
}

class R2dbcLocalDateType(override val name: String) :
    AbstractR2dbcDataType<LocalDate>(typeOf<LocalDate>(), JDBCType.DATE) {
    override fun convertBeforeGetting(value: Any): LocalDate {
        return when (value) {
            is LocalDate -> value
            is LocalDateTime -> value.toLocalDate()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: LocalDate): String {
        return "'$value'"
    }
}

class R2dbcLocalTimeType(override val name: String) :
    AbstractR2dbcDataType<LocalTime>(typeOf<LocalTime>(), JDBCType.TIME) {
    override fun convertBeforeGetting(value: Any): LocalTime {
        return when (value) {
            is LocalTime -> value
            is LocalDateTime -> value.toLocalTime()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: LocalTime): String {
        return "'$value'"
    }
}

class R2dbcLongType(override val name: String) :
    AbstractR2dbcDataType<Long>(typeOf<Long>(), JDBCType.BIGINT) {
    override fun convertBeforeGetting(value: Any): Long {
        return when (value) {
            is Number -> value.toLong()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcOffsetDateTimeType(override val name: String) :
    AbstractR2dbcDataType<OffsetDateTime>(typeOf<OffsetDateTime>(), JDBCType.TIMESTAMP_WITH_TIMEZONE) {
    override fun convertBeforeGetting(value: Any): OffsetDateTime {
        return when (value) {
            is LocalDateTime -> {
                val zoneId = ZoneId.systemDefault()
                val offset = zoneId.rules.getOffset(value)
                value.atOffset(offset)
            }
            is OffsetDateTime -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: OffsetDateTime): String {
        return "'$value'"
    }
}

class R2dbcShortType(override val name: String) :
    AbstractR2dbcDataType<Short>(typeOf<Short>(), JDBCType.SMALLINT) {
    override fun convertBeforeGetting(value: Any): Short {
        return when (value) {
            is Number -> value.toShort()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcStringType(override val name: String) :
    AbstractR2dbcDataType<String>(typeOf<String>(), JDBCType.VARCHAR) {
    override fun convertBeforeGetting(value: Any): String {
        return when (value) {
            is String -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: String): String {
        return "'$value'"
    }
}

class R2dbcUByteType(override val name: String) :
    AbstractR2dbcDataType<UByte>(typeOf<UByte>(), JDBCType.SMALLINT, Short::class.javaObjectType) {
    override fun convertBeforeGetting(value: Any): UByte {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UByte. value=$value")
            }.toUByte()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun convertBeforeBinding(value: UByte): Any {
        return value.toShort()
    }
}

class R2dbcUIntType(override val name: String) : AbstractR2dbcDataType<UInt>(typeOf<UInt>(), JDBCType.BIGINT, Long::class.javaObjectType) {
    override fun convertBeforeGetting(value: Any): UInt {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UInt. value=$value")
            }.toUInt()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun convertBeforeBinding(value: UInt): Any {
        return value.toLong()
    }
}

class R2dbcUShortType(override val name: String) :
    AbstractR2dbcDataType<UShort>(typeOf<UShort>(), JDBCType.INTEGER, Int::class.javaObjectType) {
    override fun convertBeforeGetting(value: Any): UShort {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UShort. value=$value")
            }.toUShort()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun convertBeforeBinding(value: UShort): Any {
        return value.toInt()
    }
}

class R2dbcUserDefinedDataTypeAdapter<T : Any>(private val dataType: R2dbcUserDefinedDataType<T>) : R2dbcDataType<T> {
    override val name: String get() = dataType.name

    override val type: KType get() = dataType.type

    override val sqlType: SQLType get() = dataType.sqlType

    override fun getValue(row: Row, index: Int): T? {
        return dataType.getValue(row, index)
    }

    override fun getValue(row: Row, columnLabel: String): T? {
        return dataType.getValue(row, columnLabel)
    }

    override fun setValue(statement: Statement, index: Int, value: T?) {
        if (value == null) {
            statement.bindNull(index, dataType.r2dbcType)
        } else {
            dataType.setValue(statement, index, value)
        }
    }

    override fun setValue(statement: Statement, name: String, value: T?) {
        if (value == null) {
            statement.bindNull(name, dataType.r2dbcType)
        } else {
            dataType.setValue(statement, name, value)
        }
    }

    override fun toString(value: T?): String {
        return if (value == null) "null" else dataType.toString(value)
    }
}

class R2dbcDataTypeProxy<EXTERIOR : Any, INTERIOR : Any>(
    private val converter: DataTypeConverter<EXTERIOR, INTERIOR>,
    private val dataType: R2dbcDataType<INTERIOR>,
) : R2dbcDataType<EXTERIOR> {
    override val name: String get() = dataType.name

    override val type: KType get() = converter.exteriorType

    override val sqlType: SQLType get() = dataType.sqlType

    override fun getValue(row: Row, index: Int): EXTERIOR? {
        return dataType.getValue(row, index)?.let { converter.wrap(it) }
    }

    override fun getValue(row: Row, columnLabel: String): EXTERIOR? {
        return dataType.getValue(row, columnLabel)?.let { converter.wrap(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: EXTERIOR?) {
        dataType.setValue(statement, index, value?.let { converter.unwrap(value) })
    }

    override fun setValue(statement: Statement, name: String, value: EXTERIOR?) {
        dataType.setValue(statement, name, value?.let { converter.unwrap(value) })
    }

    override fun toString(value: EXTERIOR?): String {
        return if (value == null) "null" else dataType.toString(converter.unwrap(value))
    }
}
