package org.komapper.core

import org.komapper.core.dsl.builder.DefaultEntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.DefaultEntityInsertStatementBuilder
import org.komapper.core.dsl.builder.DefaultEntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilderImpl
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.builder.RelationUpdateStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.util.regex.Pattern

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

    interface Identifier {
        val driver: String
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
     * Returns the type of LOCATE function.
     */
    fun getLocateFunctionType(): LocateFunctionType {
        return LocateFunctionType.LOCATE
    }

    /**
     * Returns the statement builder for creating the OFFSET and LIMIT expression.
     *
     * @param dialect the builder dialect
     * @param offset the offset
     * @param limit the limit
     * @return the statement builder
     */
    fun getOffsetLimitStatementBuilder(dialect: BuilderDialect, offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return OffsetLimitStatementBuilderImpl(dialect, offset, limit)
    }

    /**
     * Returns the RANDOM function.
     *
     * @return the RANDOM function
     */
    fun getRandomFunction(): String = "random"

    /**
     * Returns the SQL string for retrieving a new sequence number.
     *
     * @param sequenceName the sequence name
     * @return the SQL string
     */
    fun getSequenceSql(sequenceName: String): String

    /**
     * Returns the SUBSTRING function.
     *
     * @return the SUBSTRING function
     */
    fun getSubstringFunction(): String = "substring"

    /**
     * Returns the statement builder for schema.
     *
     * @param dialect the builder dialect
     * @return the statement builder
     */
    fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityDeleteStatementBuilder<ENTITY, ID, META> {
        return DefaultEntityDeleteStatementBuilder(dialect, context, entity)
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return DefaultEntityInsertStatementBuilder(dialect, context, entities)
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityUpdateStatementBuilder<ENTITY, ID, META> {
        return DefaultEntityUpdateStatementBuilder(dialect, context, entity)
    }

    /**
     * Returns the statement builder for the entity UPSERT command.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param dialect the builder dialect
     * @param context the context
     * @param entities the entities
     * @return the statement builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY>

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): RelationDeleteStatementBuilder<ENTITY, ID, META> {
        return RelationDeleteStatementBuilder(dialect, context)
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return RelationInsertValuesStatementBuilder(dialect, context)
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): RelationUpdateStatementBuilder<ENTITY, ID, META> {
        return RelationUpdateStatementBuilder(dialect, context)
    }

    /**
     * Returns the default length argument for the SUBSTRING function.
     *
     * @return the default length argument
     */
    fun getDefaultLengthForSubstringFunction(): Int? = null

    /**
     * Returns whether the auto-increment is supported when inserting multiple rows.
     */
    fun supportsAutoIncrementWhenInsertingMultipleRows(): Boolean = true

    fun supportsParameterBindingForWindowFrameBoundOffset(): Boolean = true

    /**
     * Returns whether the batch execution of parameterized statement is supported.
     */
    fun supportsBatchExecutionOfParameterizedStatement(): Boolean = true

    /**
     * Returns whether the batch execution returning generated values is supported.
     */
    fun supportsBatchExecutionReturningGeneratedValues(): Boolean = true

    /**
     * Returns whether the LIMIT and OFFSET clauses are supported without using the ORDER BY clause.
     */
    fun supportsLimitOffsetWithoutOrderByClause(): Boolean = true

    /**
     * Returns whether the alias is supported in the DELETE statement.
     */
    fun supportsAliasForDeleteStatement() = true

    /**
     * Returns whether the alias is supported in the UPDATE statement.
     */
    fun supportsAliasForUpdateStatement() = true

    /**
     * Returns whether the AS keyword is supported for a table alias.
     */
    fun supportsAsKeywordForTableAlias(): Boolean = true

    /**
     * Returns whether the list of column names is supported after the name of a common table expression.
     */
    fun supportsColumnNamesInCteDefinition(): Boolean = true

    /**
     * Returns whether the conflict_target is supported in the UPSERT statement.
     */
    fun supportsConflictTargetInUpsertStatement(): Boolean = false

    /**
     * Returns whether the index_predicate is supported in the UPSERT statement.
     */
    fun supportsIndexPredicateInUpsertStatement(): Boolean = false

    /**
     * Returns whether the "CREATE TABLE/SEQUENCE IF NOT EXISTS" syntax is supported.
     */
    fun supportsCreateIfNotExists(): Boolean = true

    fun supportsDeleteReturning(): Boolean = false

    /**
     * Returns whether the "DROP TABLE/SEQUENCE IF EXISTS" syntax is supported.
     */
    fun supportsDropIfExists(): Boolean = true

    fun supportsExcludedTable(): Boolean = true

    /**
     * Returns whether the generated keys returning is supported when inserting multiple rows.
     */
    fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = true

    fun supportsInsertMultipleReturning(): Boolean = false

    fun supportsInsertSingleReturning(): Boolean = false

    /**
     * Returns whether the INTERSECT set operation is supported.
     */
    fun supportsSetOperationIntersect(): Boolean = true

    /**
     * Returns whether the EXCEPT set operation is supported.
     */
    fun supportsSetOperationExcept(): Boolean = true

    /**
     * Returns whether the MINUS set operation is supported.
     */
    fun supportsSetOperationMinus(): Boolean = false

    /**
     * Returns whether the NULLS FIRST and NULLS LAST options are supported in the ORDER BY clause.
     */
    fun supportsNullOrdering(): Boolean = true

    /**
     * Returns whether the FOR UPDATE clause is supported.
     */
    fun supportsForUpdateClause(): Boolean = true

    /**
     * Returns whether the "FOR UPDATE OF ..column" syntax is supported.
     */
    fun supportsLockOfColumns(): Boolean = false

    /**
     * Returns whether the "FOR UPDATE OF ..table" syntax is supported.
     */
    fun supportsLockOfTables(): Boolean = false

    /**
     * Returns whether the NOWAIT lock option is supported.
     */
    fun supportsLockOptionNowait(): Boolean = false

    /**
     * Returns whether the SKIP LOCKED lock option is supported.
     */
    fun supportsLockOptionSkipLocked(): Boolean = false

    /**
     * Returns whether the WAIT lock option is supported.
     */
    fun supportsLockOptionWait(): Boolean = false

    /**
     * Returns whether the MOD function is supported.
     */
    fun supportsModuloOperator(): Boolean = true

    /**
     * Returns whether the multiple columns are supported in the IN predicate.
     */
    fun supportsMultipleColumnsInInPredicate(): Boolean = true

    /**
     * Returns whether the optimistic lock is supported for batch execution.
     */
    fun supportsOptimisticLockOfBatchExecution(): Boolean = true

    fun supportsRecursiveKeywordInCommonTableExpression(): Boolean = true

    fun supportsSelectStatementWithoutFromClause(): Boolean = true

    /**
     * Returns whether the search condition is supported in the UPSERT statement.
     */
    fun supportsSearchConditionInUpsertStatement(): Boolean = false

    /**
     * Returns whether the table hint is supported.
     */
    fun supportsTableHint(): Boolean = false

    fun supportsUpdateReturning(): Boolean = false

    fun supportsUpsertMultipleReturning(): Boolean = false

    fun supportsUpsertSingleReturning(): Boolean = false
}

object DryRunDialect : Dialect {
    override val driver: String = "dry_run"

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return DryRunSchemaStatementBuilder
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }

    override fun supportsLockOfColumns(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsLockOptionWait(): Boolean = true
}

/**
 * Type of LOCATE function.
 */
enum class LocateFunctionType(val functionName: String) {
    LOCATE("locate"),
    INSTR("instr"),
    CHARINDEX("charindex"),
    POSITION("position"),
}
