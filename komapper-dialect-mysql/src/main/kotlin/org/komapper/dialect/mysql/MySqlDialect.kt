package org.komapper.dialect.mysql

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface MySqlDialect : Dialect {

    companion object {
        const val DRIVER = "mysql"
    }

    override val driver: String get() = DRIVER
    override val openQuote: String get() = "`"
    override val closeQuote: String get() = "`"

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return MySqlOffsetLimitStatementBuilder(this, offset, limit)
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return MySqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MySqlEntityUpsertStatementBuilder(this, context, entities)
    }

    override fun supportsLockOfTables(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsNullOrdering(): Boolean = false
}
