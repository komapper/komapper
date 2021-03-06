package org.komapper.dialect.mysql

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface MySqlDialect : Dialect {

    companion object {
        const val driver = "mysql"
    }

    override val driver: String get() = Companion.driver
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

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MySqlEntityUpsertStatementBuilder(this, context, entities)
    }
}
