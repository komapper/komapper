package org.komapper.dialect.sqlserver

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface SqlServerDialect : Dialect {

    companion object {
        const val DRIVER = "sqlserver"
        const val OPEN_QUOTE = "["
        const val CLOSE_QUOTE = "]"
    }

    override val driver: String get() = DRIVER
    override val openQuote: String get() = OPEN_QUOTE
    override val closeQuote: String get() = CLOSE_QUOTE

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return SqlServerOffsetLimitStatementBuilder(this, offset, limit)
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return SqlServerSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return SqlServerEntityUpsertStatementBuilder(this, context, entities)
    }

    override fun getDefaultLengthForSubstringFunction(): Int? = Int.MAX_VALUE

    override fun supportsLimitOffsetWithoutOrderByClause(): Boolean = false

    override fun supportsAliasForDeleteStatement(): Boolean = false

    override fun supportsAliasForUpdateStatement(): Boolean = false

    override fun supportsNullOrdering(): Boolean = false
}
