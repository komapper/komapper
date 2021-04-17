package org.komapper.core

import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityMultiInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityMultiInsertStatementBuilderImpl
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.jdbc.AnyType
import org.komapper.core.jdbc.DataType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface Dialect {
    val openQuote: String
    val closeQuote: String
    val escapeChar: Char
    val escapePattern: Pattern

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
    fun getSequenceSql(sequenceName: String): String
    fun getOffsetLimitSql(offset: Int, limit: Int): String
    fun quote(name: String): String
    fun escape(text: String): String
    fun getSchemaStatementBuilder(): SchemaStatementBuilder
    fun <ENTITY : Any> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY>

    fun <ENTITY : Any> getEntityMultiInsertStatementBuilder(
        context: EntityInsertContext<ENTITY>,
        entities: List<ENTITY>
    ): EntityMultiInsertStatementBuilder<ENTITY>? {
        return EntityMultiInsertStatementBuilderImpl(this, context, entities)
    }
}

abstract class AbstractDialect : Dialect {

    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapeChar: Char = '\\'
    override val escapePattern: Pattern
        get() {
            @Suppress("RegExpDuplicateCharacterInClass")
            val regex = "[${escapeChar}${escapeChar}_%]"
            return Pattern.compile(regex)
        }

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val (dataType) = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any? {
        val (dataType) = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        val (dataType) = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        val (dataType) = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        return dataType.toString(value)
    }

    abstract fun getDataType(type: KClass<*>): Pair<DataType<*>, String>

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()

    override fun getOffsetLimitSql(offset: Int, limit: Int): String {
        val buf = StringBuilder(50)
        if (offset >= 0) {
            buf.append(" offset ")
            buf.append(offset)
            buf.append(" rows")
        }
        if (limit > 0) {
            buf.append(" fetch first ")
            buf.append(limit)
            buf.append(" rows only")
        }
        return buf.toString()
    }

    override fun quote(name: String): String =
        name.split('.').joinToString(".") { openQuote + it + closeQuote }

    override fun escape(text: String): String {
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("""\\$0""")
    }
}

internal object DryRunDialect : AbstractDialect() {

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getDataType(type: KClass<*>): Pair<DataType<*>, String> {
        return AnyType to "other"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return DryRunSchemaStatementBuilder
    }

    override fun <ENTITY : Any> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }
}
