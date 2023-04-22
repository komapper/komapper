package org.komapper.dialect.mariadb

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface MariaDbDialect : Dialect {

    companion object : Dialect.Identifier {
        private const val DRIVER = "mariadb"

        /** the error code that represents unique violation  */
        val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = setOf(1022, 1062)
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER
    override val openQuote: String get() = "`"
    override val closeQuote: String get() = "`"

    override fun getOffsetLimitStatementBuilder(dialect: BuilderDialect, offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return MariaDbOffsetLimitStatementBuilder(dialect, offset, limit)
    }

    override fun getRandomFunction(): String = "rand"

    override fun getSequenceSql(sequenceName: String): String {
        return "select next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return MariaDbSchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return MariaDbEntityInsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MariaDbEntityUpsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return MariaDbRelationInsertValuesStatementBuilder(dialect, context)
    }

    override fun supportsAliasForDeleteStatement() = false

    override fun supportsConflictTargetInUpsertStatement(): Boolean = false

    override fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = false

    override fun supportsInsertReturning(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsLockOptionWait(): Boolean = true

    override fun supportsNullOrdering(): Boolean = false

    override fun supportsOptimisticLockOfBatchExecution(): Boolean = false

    override fun supportsSearchConditionInUpsertStatement(): Boolean = false
}
