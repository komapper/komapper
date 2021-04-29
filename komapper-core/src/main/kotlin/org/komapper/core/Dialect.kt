package org.komapper.core

import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilderImpl
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.jdbc.AnyType
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DialectFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ServiceLoader
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface Dialect {
    val subprotocol: String
    val openQuote: String
    val closeQuote: String
    val escapeSequence: String

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun getDataType(klass: KClass<*>): DataType<*>
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
    fun getSequenceSql(sequenceName: String): String
    fun enquote(name: String): String
    fun escape(text: String, escapeSequence: String? = null): String
    fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder
    fun getSchemaStatementBuilder(): SchemaStatementBuilder

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY>

    companion object {
        private val jdbcUrlPattern = Pattern.compile("^jdbc:([^:]*):.*")

        fun load(url: String, dataTypes: Set<DataType<*>>): Dialect {
            val subprotocol = extractJdbcSubprotocol(url)
            val loader = ServiceLoader.load(DialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(subprotocol) }
                ?: error(
                    "The dialect is not found for the JDBC url. " +
                        "Try to add the 'komapper-jdbc-$subprotocol' dependency. " +
                        "url=$url, subprotocol='$subprotocol'"
                )
            return factory.create(dataTypes)
        }

        private fun extractJdbcSubprotocol(url: String): String {
            val matcher = jdbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).lowercase()
            }
            error("The subprotocol in the JDBC URL is not found. url=$url")
        }
    }
}

abstract class AbstractDialect protected constructor(dataTypes: Set<DataType<*>> = emptySet()) : Dialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, DataType<*>> = dataTypes.associateBy { it.klass }
    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapeSequence: String = "\\"

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        return dataType.toString(value)
    }

    override fun getDataType(klass: KClass<*>): DataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

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

    @Suppress("RegExpDuplicateCharacterInClass")
    protected open fun createEscapePattern(escapeSequence: String): Pattern {
        val targetChars = "[${Regex.escape("$escapeSequence%_")}]"
        return Pattern.compile(targetChars)
    }

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return OffsetLimitStatementBuilderImpl(this, offset, limit)
    }
}

internal object DryRunDialect : AbstractDialect() {

    override val subprotocol: String = "dry_run"

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getDataType(klass: KClass<*>): DataType<Any> {
        return AnyType("other")
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return DryRunSchemaStatementBuilder
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }
}
