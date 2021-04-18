package org.komapper.core

import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityMultiInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityMultiInsertStatementBuilderImpl
import org.komapper.core.dsl.builder.EntityMultiUpsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilderImpl
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
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
    val escapeSequence: String

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
    fun getSequenceSql(sequenceName: String): String
    fun enquote(name: String): String
    fun escape(text: String, escapeSequence: String? = null): String

    fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder

    fun getSchemaStatementBuilder(): SchemaStatementBuilder

    fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY>

    fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityMultiInsertStatementBuilder(
        context: EntityInsertContext<ENTITY, META>,
        entities: List<ENTITY>
    ): EntityMultiInsertStatementBuilder<ENTITY>?

    fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityMultiUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entities: List<ENTITY>
    ): EntityMultiUpsertStatementBuilder<ENTITY>?
}

abstract class AbstractDialect : Dialect {

    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapeSequence: String = "\\"

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

    override fun enquote(name: String): String {
        return openQuote + name + closeQuote
    }

    override fun escape(text: String, escapeSequence: String?): String {
        val literal = escapeSequence ?: this.escapeSequence
        val escapePattern = createEscapePattern(literal)
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("${Regex.escapeReplacement(literal)}$0")
    }

    protected open fun createEscapePattern(escapeSequence: String): Pattern {
        val targetChars = "[${Regex.escape("$escapeSequence%_")}]"
        return Pattern.compile(targetChars)
    }

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return OffsetLimitStatementBuilderImpl(this, offset, limit)
    }

    override fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityMultiInsertStatementBuilder(
        context: EntityInsertContext<ENTITY, META>,
        entities: List<ENTITY>
    ): EntityMultiInsertStatementBuilder<ENTITY>? {
        return EntityMultiInsertStatementBuilderImpl(this, context, entities)
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

    override fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }

    override fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityMultiUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entities: List<ENTITY>
    ): EntityMultiUpsertStatementBuilder<ENTITY>? {
        throw UnsupportedOperationException()
    }
}
