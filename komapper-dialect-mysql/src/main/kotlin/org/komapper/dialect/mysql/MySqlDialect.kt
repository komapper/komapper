package org.komapper.dialect.mysql

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface MySqlDialect : Dialect {
    companion object : Dialect.Identifier {
        const val DRIVER = "mysql"

        /** the error code that represents unique violation  */
        val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = setOf(1022, 1062)
        override val driver: String = DRIVER
    }

    val version: MySqlVersion

    override val driver: String get() = DRIVER
    override val openQuote: String get() = "`"
    override val closeQuote: String get() = "`"

    override fun getOffsetLimitStatementBuilder(
        dialect: BuilderDialect,
        offset: Int,
        limit: Int,
    ): OffsetLimitStatementBuilder {
        return MySqlOffsetLimitStatementBuilder(dialect, offset, limit)
    }

    override fun getRandomFunction(): String = "rand"

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return MySqlSchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MySqlEntityUpsertStatementBuilder(dialect, context, entities, version)
    }

    override fun supportsAliasForDeleteStatement(): Boolean = when (version) {
        MySqlVersion.V5 -> false
        MySqlVersion.V8 -> true
    }

    override fun supportsConflictTargetInUpsertStatement(): Boolean = false

    override fun supportsExcludedTable(): Boolean = when (version) {
        MySqlVersion.V5 -> false
        MySqlVersion.V8 -> true
    }

    override fun supportsLockOfTables(): Boolean = when (version) {
        MySqlVersion.V5 -> false
        MySqlVersion.V8 -> true
    }

    override fun supportsLockOptionNowait(): Boolean = when (version) {
        MySqlVersion.V5 -> false
        MySqlVersion.V8 -> true
    }

    override fun supportsLockOptionSkipLocked(): Boolean = when (version) {
        MySqlVersion.V5 -> false
        MySqlVersion.V8 -> true
    }

    override fun supportsNullOrdering(): Boolean = false

    override fun supportsSearchConditionInUpsertStatement(): Boolean = false

    override fun supportsSetOperationIntersect(): Boolean = false

    override fun supportsSetOperationExcept(): Boolean = false
}
