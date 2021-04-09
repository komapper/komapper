package org.komapper.core

import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.jdbc.AnyType
import org.komapper.core.jdbc.ArrayType
import org.komapper.core.jdbc.BigDecimalType
import org.komapper.core.jdbc.BigIntegerType
import org.komapper.core.jdbc.BlobType
import org.komapper.core.jdbc.BooleanType
import org.komapper.core.jdbc.ByteArrayType
import org.komapper.core.jdbc.ByteType
import org.komapper.core.jdbc.ClobType
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.DoubleType
import org.komapper.core.jdbc.FloatType
import org.komapper.core.jdbc.IntType
import org.komapper.core.jdbc.LocalDateTimeType
import org.komapper.core.jdbc.LocalDateType
import org.komapper.core.jdbc.LocalTimeType
import org.komapper.core.jdbc.LongType
import org.komapper.core.jdbc.NClobType
import org.komapper.core.jdbc.OffsetDateTimeType
import org.komapper.core.jdbc.SQLXMLType
import org.komapper.core.jdbc.ShortType
import org.komapper.core.jdbc.StringType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface Dialect {
    val openQuote: String
    val closeQuote: String
    val escapePattern: Pattern
    val schemaStatementBuilder: SchemaStatementBuilder

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
    fun getSequenceSql(sequenceName: String): String
    fun quote(name: String): String
    fun supportsMerge(): Boolean
    fun supportsUpsert(): Boolean
    fun escape(text: String): String
}

abstract class AbstractDialect : Dialect {

    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapePattern: Pattern = Pattern.compile("""[\\_%]""")

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        @Suppress("UNCHECKED_CAST")
        val dataType = getDataType(valueClass) as DataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        @Suppress("UNCHECKED_CAST")
        val dataType = getDataType(valueClass) as DataType<Any>
        return dataType.toString(value)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun getDataType(type: KClass<*>): DataType<*> = when (type) {
        Any::class -> AnyType
        java.sql.Array::class -> ArrayType
        BigDecimal::class -> BigDecimalType
        BigInteger::class -> BigIntegerType
        Blob::class -> BlobType
        Boolean::class -> BooleanType
        Byte::class -> ByteType
        ByteArray::class -> ByteArrayType
        Double::class -> DoubleType
        Clob::class -> ClobType
        Float::class -> FloatType
        Int::class -> IntType
        LocalDateTime::class -> LocalDateTimeType
        LocalDate::class -> LocalDateType
        LocalTime::class -> LocalTimeType
        Long::class -> LongType
        NClob::class -> NClobType
        OffsetDateTime::class -> OffsetDateTimeType
        Short::class -> ShortType
        String::class -> StringType
        SQLXML::class -> SQLXMLType
        else -> error(
            "The dataType is not found for the type \"${type.qualifiedName}\"."
        )
    }

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()

    override fun quote(name: String): String =
        name.split('.').joinToString(".") { openQuote + it + closeQuote }

    override fun supportsMerge(): Boolean = false

    override fun supportsUpsert(): Boolean = false

    override fun escape(text: String): String {
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("""\\$0""")
    }
}

internal object DryRunDialect : AbstractDialect() {

    override val schemaStatementBuilder = DryRunSchemaStatementBuilder

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }
}
