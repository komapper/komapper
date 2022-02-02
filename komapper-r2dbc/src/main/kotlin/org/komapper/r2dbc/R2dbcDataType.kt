package org.komapper.r2dbc

import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.ThreadSafe
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.reflect.KClass

/**
 * Represents a data type for R2DBC access.
 */
@ThreadSafe
interface R2dbcDataType<T : Any> {
    /**
     * The data type name.
     */
    val name: String

    /**
     * The corresponding class.
     */
    val klass: KClass<T>

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

abstract class R2dbcAbstractDataType<T : Any>(
    override val klass: KClass<T>,
) : R2dbcDataType<T> {

    override fun getValue(row: Row, index: Int): T? {
        return row[index]?.let { convert(it) }
    }

    override fun getValue(row: Row, columnLabel: String): T? {
        return row[columnLabel]?.let { convert(it) }
    }

    protected open fun convert(value: Any): T {
        throw UnsupportedOperationException()
    }

    override fun setValue(statement: Statement, index: Int, value: T?) {
        if (value == null) {
            statement.bindNull(index, klass.javaObjectType)
        } else {
            bind(statement, index, value)
        }
    }

    override fun setValue(statement: Statement, name: String, value: T?) {
        if (value == null) {
            statement.bindNull(name, klass.javaObjectType)
        } else {
            bind(statement, name, value)
        }
    }

    protected open fun bind(statement: Statement, index: Int, value: T) {
        statement.bind(index, value)
    }

    protected open fun bind(statement: Statement, name: String, value: T) {
        statement.bind(name, value)
    }

    override fun toString(value: T?): String {
        return if (value == null) "null" else doToString(value)
    }

    protected open fun doToString(value: T): String {
        return value.toString()
    }
}

class R2dbcAnyType(override val name: String) :
    R2dbcAbstractDataType<Any>(Any::class) {
    override fun convert(value: Any): Any {
        return value
    }
}

class R2dbcArrayType(override val name: String) :
    R2dbcAbstractDataType<Array>(Array::class) {
    override fun convert(value: Any): Array {
        return when (value) {
            is Array -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcBigDecimalType(override val name: String) :
    R2dbcAbstractDataType<BigDecimal>(BigDecimal::class) {
    override fun convert(value: Any): BigDecimal {
        return when (value) {
            is BigDecimal -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcBigIntegerType(override val name: String) : R2dbcDataType<BigInteger> {
    private val dataType = R2dbcBigDecimalType(name)
    override val klass: KClass<BigInteger> = BigInteger::class

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
    R2dbcAbstractDataType<Blob>(Blob::class) {

    override fun getValue(row: Row, index: Int): Blob? {
        return row.get(index, klass.java)
    }

    override fun getValue(row: Row, columnLabel: String): Blob? {
        return row.get(columnLabel, klass.java)
    }
}

class R2dbcBooleanType(override val name: String) :
    R2dbcAbstractDataType<Boolean>(Boolean::class) {

    override fun convert(value: Any): Boolean {
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
    R2dbcAbstractDataType<Byte>(Byte::class) {
    override fun convert(value: Any): Byte {
        return when (value) {
            is Number -> value.toByte()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcByteArrayType(override val name: String) :
    R2dbcAbstractDataType<ByteArray>(ByteArray::class) {
    override fun convert(value: Any): ByteArray {
        return when (value) {
            is ByteArray -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcClobType(override val name: String) :
    R2dbcAbstractDataType<Clob>(Clob::class) {

    override fun getValue(row: Row, index: Int): Clob? {
        return row.get(index, klass.java)
    }

    override fun getValue(row: Row, columnLabel: String): Clob? {
        return row.get(columnLabel, klass.java)
    }
}

class R2dbcDoubleType(override val name: String) :
    R2dbcAbstractDataType<Double>(Double::class) {
    override fun convert(value: Any): Double {
        return when (value) {
            is Number -> value.toDouble()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcFloatType(override val name: String) :
    R2dbcAbstractDataType<Float>(Float::class) {
    override fun convert(value: Any): Float {
        return when (value) {
            is Number -> value.toFloat()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcIntType(override val name: String) :
    R2dbcAbstractDataType<Int>(Int::class) {

    override fun convert(value: Any): Int {
        return when (value) {
            is Number -> value.toInt()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcLocalDateTimeType(override val name: String) :
    R2dbcAbstractDataType<LocalDateTime>(LocalDateTime::class) {

    override fun convert(value: Any): LocalDateTime {
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
    R2dbcAbstractDataType<LocalDate>(LocalDate::class) {

    override fun convert(value: Any): LocalDate {
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
    R2dbcAbstractDataType<LocalTime>(LocalTime::class) {

    override fun convert(value: Any): LocalTime {
        return when (value) {
            is LocalTime -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: LocalTime): String {
        return "'$value'"
    }
}

class R2dbcLongType(override val name: String) :
    R2dbcAbstractDataType<Long>(Long::class) {
    override fun convert(value: Any): Long {
        return when (value) {
            is Number -> value.toLong()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcOffsetDateTimeType(override val name: String) :
    R2dbcAbstractDataType<OffsetDateTime>(OffsetDateTime::class) {

    override fun convert(value: Any): OffsetDateTime {
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
    R2dbcAbstractDataType<Short>(Short::class) {
    override fun convert(value: Any): Short {
        return when (value) {
            is Number -> value.toShort()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class R2dbcStringType(override val name: String) :
    R2dbcAbstractDataType<String>(String::class) {

    override fun convert(value: Any): String {
        return when (value) {
            is String -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: String): String {
        return "'$value'"
    }
}

class R2dbcUByteType(override val name: String) : R2dbcAbstractDataType<UByte>(UByte::class) {
    override fun convert(value: Any): UByte {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UByte. value=$value")
            }.toUByte()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun bind(statement: Statement, index: Int, value: UByte) {
        statement.bind(index, value.toShort())
    }
}

class R2dbcUIntType(override val name: String) : R2dbcAbstractDataType<UInt>(UInt::class) {
    override fun convert(value: Any): UInt {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UInt. value=$value")
            }.toUInt()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun bind(statement: Statement, index: Int, value: UInt) {
        statement.bind(index, value.toLong())
    }
}

class R2dbcUShortType(override val name: String) : R2dbcAbstractDataType<UShort>(UShort::class) {
    override fun convert(value: Any): UShort {
        return when (value) {
            is Number -> value.toLong().also {
                if (it < 0L) error("Negative value isn't convertible to UShort. value=$value")
            }.toUShort()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun bind(statement: Statement, index: Int, value: UShort) {
        statement.bind(index, value.toInt())
    }
}
