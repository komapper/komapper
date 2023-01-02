package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
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

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return SqlServerEntityUpsertStatementBuilder(dialect, context, entities)
    }

    override fun getDefaultLengthForSubstringFunction(): Int? = Int.MAX_VALUE

    override fun supportsLimitOffsetWithoutOrderByClause(): Boolean = false

    override fun supportsAliasForDeleteStatement(): Boolean = false

    override fun supportsAliasForUpdateStatement(): Boolean = false

    override fun supportsCreateIfNotExists(): Boolean = false

    override fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = false

    override fun supportsNullOrdering(): Boolean = false

    override fun supportsForUpdateClause(): Boolean = false

    override fun supportsMultipleColumnsInInPredicate(): Boolean = false

    override fun supportsTableHint(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true
}
