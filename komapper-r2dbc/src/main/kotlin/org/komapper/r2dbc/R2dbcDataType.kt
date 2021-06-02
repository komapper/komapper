package org.komapper.r2dbc

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
import kotlin.reflect.KClass

@ThreadSafe
interface R2dbcDataType<T : Any> {
    val name: String
    val klass: KClass<T>

    fun getValue(row: Row, index: Int): T?
    fun getValue(row: Row, columnLabel: String): T?
    fun setValue(statement: Statement, index: Int, value: T?)
    fun setValue(statement: Statement, name: String, value: T?)
    fun toString(value: T?): String
}

abstract class AbstractDataType<T : Any>(
    override val klass: KClass<T>,
) : R2dbcDataType<T> {

    override fun getValue(row: Row, index: Int): T? {
        return row[index]?.let { convert(it) }
    }

    override fun getValue(row: Row, columnLabel: String): T? {
        return row[columnLabel]?.let { convert(it) }
    }

    protected abstract fun convert(value: Any): T

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

class AnyType(override val name: String) :
    AbstractDataType<Any>(Any::class) {
    override fun convert(value: Any): Any {
        return value
    }
}

class ArrayType(override val name: String) :
    AbstractDataType<Array>(Array::class) {
    override fun convert(value: Any): Array {
        return when (value) {
            is Array -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class BigDecimalType(override val name: String) :
    AbstractDataType<BigDecimal>(BigDecimal::class) {
    override fun convert(value: Any): BigDecimal {
        return when (value) {
            is BigDecimal -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class BigIntegerType(override val name: String) : R2dbcDataType<BigInteger> {
    private val dataType = BigDecimalType(name)
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

class BooleanType(override val name: String) :
    AbstractDataType<Boolean>(Boolean::class) {

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

class ByteType(override val name: String) :
    AbstractDataType<Byte>(Byte::class) {
    override fun convert(value: Any): Byte {
        return when (value) {
            is Number -> value.toByte()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class ByteArrayType(override val name: String) :
    AbstractDataType<ByteArray>(ByteArray::class) {
    override fun convert(value: Any): ByteArray {
        return when (value) {
            is ByteArray -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class DoubleType(override val name: String) :
    AbstractDataType<Double>(Double::class) {
    override fun convert(value: Any): Double {
        return when (value) {
            is Number -> value.toDouble()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class FloatType(override val name: String) :
    AbstractDataType<Float>(Float::class) {
    override fun convert(value: Any): Float {
        return when (value) {
            is Number -> value.toFloat()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class IntType(override val name: String) :
    AbstractDataType<Int>(Int::class) {

    override fun convert(value: Any): Int {
        return when (value) {
            is Number -> value.toInt()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class LocalDateTimeType(override val name: String) :
    AbstractDataType<LocalDateTime>(LocalDateTime::class) {

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

class LocalDateType(override val name: String) :
    AbstractDataType<LocalDate>(LocalDate::class) {

    override fun convert(value: Any): LocalDate {
        return when (value) {
            is LocalDate -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: LocalDate): String {
        return "'$value'"
    }
}

class LocalTimeType(override val name: String) :
    AbstractDataType<LocalTime>(LocalTime::class) {

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

class LongType(override val name: String) :
    AbstractDataType<Long>(Long::class) {
    override fun convert(value: Any): Long {
        return when (value) {
            is Number -> value.toLong()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class OffsetDateTimeType(override val name: String) :
    AbstractDataType<OffsetDateTime>(OffsetDateTime::class) {

    override fun convert(value: Any): OffsetDateTime {
        return when (value) {
            is OffsetDateTime -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: OffsetDateTime): String {
        return "'$value'"
    }
}

class ShortType(override val name: String) :
    AbstractDataType<Short>(Short::class) {
    override fun convert(value: Any): Short {
        return when (value) {
            is Number -> value.toShort()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

class StringType(override val name: String) :
    AbstractDataType<String>(String::class) {

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

class UByteType(override val name: String) : AbstractDataType<UByte>(UByte::class) {
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

class UIntType(override val name: String) : AbstractDataType<UInt>(UInt::class) {
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

class UShortType(override val name: String) : AbstractDataType<UShort>(UShort::class) {
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
