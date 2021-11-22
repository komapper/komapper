package org.komapper.core

import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilderImpl
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.util.regex.Pattern
import kotlin.reflect.KClass

@ThreadSafe
interface Dialect {
    val driver: String
    val openQuote: String get() = "\""
    val closeQuote: String get() = "\""
    val escapeSequence: String get() = "\\"

    fun replacePlaceHolder(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence {
        return placeHolder
    }

    fun formatValue(value: Any?, valueClass: KClass<*>): String

    fun enquote(name: String): String {
        return openQuote + name + closeQuote
    }

    fun escape(text: String, escapeSequence: String? = null): String {
        val literal = escapeSequence ?: this.escapeSequence
        val escapePattern = createEscapePattern(literal)
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("${Regex.escapeReplacement(literal)}$0")
    }

    @Suppress("RegExpDuplicateCharacterInClass")
    fun createEscapePattern(escapeSequence: String): Pattern {
        val targetChars = "[${Regex.escape("$escapeSequence%_")}]"
        return Pattern.compile(targetChars)
    }

    fun getDataTypeName(klass: KClass<*>): String

    fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return OffsetLimitStatementBuilderImpl(this, offset, limit)
    }

    fun getSequenceSql(sequenceName: String): String

    fun getSchemaStatementBuilder(): SchemaStatementBuilder

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY>

    fun supportsAliasForDeleteStatement() = true

    fun supportsNullOrdering(): Boolean = true
}

internal object DryRunDialect : Dialect {

    override val driver: String = "dry_run"

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        return if (value == null) {
            "null"
        } else {
            when (valueClass) {
                String::class -> "'$value'"
                else -> value.toString()
            }
        }
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return DryRunSchemaStatementBuilder
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }
}
