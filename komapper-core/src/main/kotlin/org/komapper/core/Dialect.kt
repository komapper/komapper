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

/**
 * Dialect for database.
 */
@ThreadSafe
interface Dialect {

    companion object {
        const val OPEN_QUOTE = "\""
        const val CLOSE_QUOTE = "\""
        const val ESCAPE_SEQUENCE = "\\"
        const val MASK = "*****"
    }

    /**
     * The name of the driver.
     */
    val driver: String

    /**
     * The open quote for identifiers.
     */
    val openQuote: String get() = OPEN_QUOTE

    /**
     * The close quote for identifiers.
     */
    val closeQuote: String get() = CLOSE_QUOTE

    /**
     * The escape sequence to be used in the LIKE predicate.
     */
    val escapeSequence: String get() = ESCAPE_SEQUENCE

    /**
     * The mask string used to hide sensitive information contained in logs.
     */
    val mask: String get() = MASK

    /**
     * Creates a bind variable name.
     *
     * @param index the binding index
     * @param value the value
     * @return the bind variable name
     */
    fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence {
        return Statement.createBindVariable(index, value)
    }

    /**
     * Formats the value.
     *
     * @param value the value
     * @param valueClass the class of the value
     * @param masking whether to mask the value
     * @return the formatted value
     */
    fun formatValue(value: Any?, valueClass: KClass<*>, masking: Boolean): String

    /**
     * Enclose the sql identifier with the [openQuote] and the [closeQuote].
     *
     * @param name the identifier
     * @return the quoted identifier
     */
    fun enquote(name: String): String {
        return openQuote + name + closeQuote
    }

    /**
     * Escapes the operand value of the LIKE predicate with the escape sequence.
     *
     * @param text the value
     * @param escapeSequence the escape sequence
     * @return the escaped value
     */
    fun escape(text: String, escapeSequence: String? = null): String {
        val literal = escapeSequence ?: this.escapeSequence
        val escapePattern = createEscapePattern(literal)
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("${Regex.escapeReplacement(literal)}$0")
    }

    /**
     * Creates a regex pattern to escape the operand value of the LIKE predicate.
     *
     * @param escapeSequence the escape sequence
     * @return the regex pattern
     */
    @Suppress("RegExpDuplicateCharacterInClass")
    fun createEscapePattern(escapeSequence: String): Pattern {
        val targetChars = "[${Regex.escape("$escapeSequence%_")}]"
        return Pattern.compile(targetChars)
    }

    /**
     * Returns the data type name.
     *
     * @param klass the class corresponding the data type
     * @return the data type name
     */
    fun getDataTypeName(klass: KClass<*>): String

    /**
     * Returns the statement builder for creating the offset and limit expression.
     *
     * @param offset the offset
     * @param limit the limit
     * @return the statement builder
     */
    fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return OffsetLimitStatementBuilderImpl(this, offset, limit)
    }

    /**
     * Returns the SQL string for retrieving a new sequence number.
     *
     * @param sequenceName the sequence name
     * @return the SQL string
     */
    fun getSequenceSql(sequenceName: String): String

    /**
     * Returns the statement builder for schema.
     *
     * @return the statement builder
     */
    fun getSchemaStatementBuilder(): SchemaStatementBuilder

    /**
     * Returns the statement builder for the entity upsert command.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param context the context
     * @param entities the entities
     * @return the statement builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY>

    /**
     * Returns the default length argument for the substring function.
     *
     * @return the default length argument
     */
    fun getDefaultLengthForSubstringFunction(): Int? = null

    /**
     * Returns whether the limit and offset clauses are supported without using the order by clause.
     *
     * @return whether the limit and offset clauses are supported
     */
    fun supportsLimitOffsetWithoutOrderByClause(): Boolean = true

    /**
     * Returns whether the alias is supported in the delete statement.
     *
     * @return whether the alias is supported
     */
    fun supportsAliasForDeleteStatement() = true

    /**
     * Returns whether the alias is supported in the update statement.
     *
     * @return whether the alias is supported
     */
    fun supportsAliasForUpdateStatement() = true

    /**
     * Returns whether the nulls first and nulls last options are supported in the order by clause.
     *
     * @return whether the nulls first and nulls last options are supported
     */
    fun supportsNullOrdering(): Boolean = true

    /**
     * Returns whether the for update clause is supported.
     *
     * @return whether the for update clause is supported
     */
    fun supportsForUpdateClause(): Boolean = true

    /**
     * Returns whether the multiple columns are supported in the in predicate.
     *
     * @return whether the multiple columns are supported
     */
    fun supportsMultipleColumnsInInPredicate(): Boolean = true

    /**
     * Returns whether the table hint is supported.
     *
     * @return whether the table hint is supported
     */
    fun supportsTableHint(): Boolean = false
}

object DryRunDialect : Dialect {

    override val driver: String = "dry_run"

    override fun formatValue(value: Any?, valueClass: KClass<*>, masking: Boolean): String {
        return if (masking) {
            mask
        } else if (value == null) {
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
