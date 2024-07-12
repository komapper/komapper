package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.LocateFunctionType
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityMergeStatementBuilder
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
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

interface SqlServerDialect : Dialect {

    companion object : Dialect.Identifier {
        private const val DRIVER = "sqlserver"
        const val OPEN_QUOTE = "["
        const val CLOSE_QUOTE = "]"

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 2627
        const val OBJECT_ALREADY_EXISTS_ERROR_CODE = 2714
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER
    override val openQuote: String get() = OPEN_QUOTE
    override val closeQuote: String get() = CLOSE_QUOTE

    override fun getLocateFunctionType(): LocateFunctionType {
        return LocateFunctionType.CHARINDEX
    }

    override fun getOffsetLimitStatementBuilder(
        dialect: BuilderDialect,
        offset: Int,
        limit: Int,
    ): OffsetLimitStatementBuilder {
        return SqlServerOffsetLimitStatementBuilder(dialect, offset, limit)
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return SqlServerSchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityDeleteStatementBuilder<ENTITY, ID, META> {
        return SqlServerEntityDeleteStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return SqlServerEntityInsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityUpdateStatementBuilder<ENTITY, ID, META> {
        return SqlServerEntityUpdateStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        val insertStatementBuilder = SqlServerEntityInsertStatementBuilder(dialect, context.insertContext, entities)
        val upsertStatementBuilder = SqlServerEntityUpsertStatementBuilder(dialect, context, entities)
        return EntityMergeStatementBuilder(dialect, context, insertStatementBuilder, upsertStatementBuilder)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): RelationDeleteStatementBuilder<ENTITY, ID, META> {
        return SqlServerRelationDeleteStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return SqlServerRelationInsertValuesStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): RelationUpdateStatementBuilder<ENTITY, ID, META> {
        return SqlServerRelationUpdateStatementBuilder(dialect, context)
    }

    override fun getDefaultLengthForSubstringFunction(): Int? = Int.MAX_VALUE

    override fun getRandomFunction(): String = "rand"

    override fun supportsParameterBindingForWindowFrameBoundOffset(): Boolean = false

    override fun supportsConflictTargetInUpsertStatement(): Boolean = false

    override fun supportsDeleteReturning(): Boolean = true

    override fun supportsLimitOffsetWithoutOrderByClause(): Boolean = false

    override fun supportsAliasForDeleteStatement(): Boolean = false

    override fun supportsAliasForUpdateStatement(): Boolean = false

    override fun supportsCreateIfNotExists(): Boolean = false

    override fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = false

    override fun supportsNullOrdering(): Boolean = false

    override fun supportsForUpdateClause(): Boolean = false

    override fun supportsInsertMultipleReturning(): Boolean = true

    override fun supportsInsertSingleReturning(): Boolean = true

    override fun supportsMultipleColumnsInInPredicate(): Boolean = false

    override fun supportsTableHint(): Boolean = true

    override fun supportsUpdateReturning(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsRecursiveKeywordInCommonTableExpression(): Boolean = false

    override fun supportsSearchConditionInUpsertStatement(): Boolean = true

    override fun supportsUpsertMultipleReturning(): Boolean = true

    override fun supportsUpsertSingleReturning(): Boolean = true
}
