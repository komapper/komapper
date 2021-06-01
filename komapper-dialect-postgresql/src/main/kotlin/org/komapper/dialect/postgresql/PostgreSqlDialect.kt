package org.komapper.dialect.postgresql

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface PostgreSqlDialect : Dialect {

    companion object {
        const val driver = "postgresql"
    }

    override val driver: String get() = Companion.driver

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return PostgreSqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return PostgreSqlEntityUpsertStatementBuilder(this, context, entities)
    }
}
