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
interface DataType<T : Any> {
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
) : DataType<T> {

    override fun getValue(row: Row, index: Int): T? {
        return doGetValue(row, index)
    }

    override fun getValue(row: Row, columnLabel: String): T? {
        return doGetValue(row, columnLabel)
    }

    protected open fun doGetValue(row: Row, index: Int): T? {
        return row.get(index, klass.javaObjectType)
    }

    protected open fun doGetValue(row: Row, columnLabel: String): T? {
        return row.get(columnLabel, klass.javaObjectType)
    }

    override fun setValue(statement: Statement, index: Int, value: T?) {
        if (value == null) {
            statement.bindNull(index, klass.javaObjectType)
        } else {
            doSetValue(statement, index, value)
        }
    }

    override fun setValue(statement: Statement, name: String, value: T?) {
        if (value == null) {
            statement.bindNull(name, klass.javaObjectType)
        } else {
            doSetValue(statement, name, value)
        }
    }

    protected open fun doSetValue(statement: Statement, index: Int, value: T) {
        statement.bind(index, value)
    }

    protected open fun doSetValue(statement: Statement, name: String, value: T) {
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
    AbstractDataType<Any>(Any::class)

class ArrayType(override val name: String) :
    AbstractDataType<Array>(Array::class)

class BigDecimalType(override val name: String) :
    AbstractDataType<BigDecimal>(BigDecimal::class)

class BigIntegerType(override val name: String) : DataType<BigInteger> {
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

    override fun doToString(value: Boolean): String {
        return value.toString().uppercase()
    }
}

class ByteType(override val name: String) :
    AbstractDataType<Byte>(Byte::class)

class ByteArrayType(override val name: String) :
    AbstractDataType<ByteArray>(ByteArray::class)

class DoubleType(override val name: String) :
    AbstractDataType<Double>(Double::class)

class FloatType(override val name: String) :
    AbstractDataType<Float>(Float::class)

class IntType(override val name: String) :
    AbstractDataType<Int>(Int::class)

class LocalDateTimeType(override val name: String) :
    AbstractDataType<LocalDateTime>(LocalDateTime::class) {

    override fun doToString(value: LocalDateTime): String {
        return "'$value'"
    }
}

class LocalDateType(override val name: String) :
    AbstractDataType<LocalDate>(LocalDate::class) {

    override fun doToString(value: LocalDate): String {
        return "'$value'"
    }
}

class LocalTimeType(override val name: String) :
    AbstractDataType<LocalTime>(LocalTime::class) {

    override fun doToString(value: LocalTime): String {
        return "'$value'"
    }
}

class LongType(override val name: String) :
    AbstractDataType<Long>(Long::class)

class OffsetDateTimeType(override val name: String) :
    AbstractDataType<OffsetDateTime>(OffsetDateTime::class) {

    override fun doToString(value: OffsetDateTime): String {
        return "'$value'"
    }
}

class ShortType(override val name: String) :
    AbstractDataType<Short>(Short::class)

class StringType(override val name: String) :
    AbstractDataType<String>(String::class) {

    override fun doToString(value: String): String {
        return "'$value'"
    }
}

class UByteType(override val name: String) : AbstractDataType<UByte>(UByte::class) {
    override fun doGetValue(row: Row, index: Int): UByte {
        val value = row.get(index, Short::class.java)
        if (value < 0) error("Negative value isn't convertible to UByte. index=$index, value=$value")
        return value.toUByte()
    }

    override fun doGetValue(row: Row, columnLabel: String): UByte {
        val value = row.get(columnLabel, Short::class.java)
        if (value < 0) error("Negative value isn't convertible to UByte. columnLabel=$columnLabel, value=$value")
        return value.toUByte()
    }

    override fun doSetValue(statement: Statement, index: Int, value: UByte) {
        statement.bind(index, value.toShort())
    }
}

class UIntType(override val name: String) : AbstractDataType<UInt>(UInt::class) {
    override fun doGetValue(row: Row, index: Int): UInt {
        val value = row.get(index, Long::class.java)
        if (value < 0L) error("Negative value isn't convertible to UInt. index=$index, value=$value")
        return value.toUInt()
    }

    override fun doGetValue(row: Row, columnLabel: String): UInt {
        val value = row.get(columnLabel, Long::class.java)
        if (value < 0L) error("Negative value isn't convertible to UInt. columnLabel=$columnLabel, value=$value")
        return value.toUInt()
    }

    override fun doSetValue(statement: Statement, index: Int, value: UInt) {
        statement.bind(index, value.toLong())
    }
}

class UShortType(override val name: String) : AbstractDataType<UShort>(UShort::class) {
    override fun doGetValue(row: Row, index: Int): UShort {
        val value = row.get(index, Int::class.java)
        if (value < 0L) error("Negative value isn't convertible to UShort. index=$index, value=$value")
        return value.toUShort()
    }

    override fun doGetValue(row: Row, columnLabel: String): UShort {
        val value = row.get(columnLabel, Int::class.java)
        if (value < 0L) error("Negative value isn't convertible to UShort. columnLabel=$columnLabel, value=$value")
        return value.toUShort()
    }

    override fun doSetValue(statement: Statement, index: Int, value: UShort) {
        statement.bind(index, value.toInt())
    }
}
