package org.komapper.dialect.mariadb

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface MariaDbDialect : Dialect {

    companion object {
        const val DRIVER = "mariadb"
    }

    override val driver: String get() = DRIVER
    override val openQuote: String get() = "`"
    override val closeQuote: String get() = "`"

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return MariaDbOffsetLimitStatementBuilder(this, offset, limit)
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return MariaDbSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MariaDbEntityUpsertStatementBuilder(this, context, entities)
    }

    override fun supportsAliasForDeleteStatement() = false

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsLockOptionWait(): Boolean = true

    override fun supportsNullOrdering(): Boolean = false
}
