package org.komapper.dialect.oracle

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface OracleDialect : Dialect {

    companion object {
        const val DRIVER = "oracle"
    }

    override val driver: String get() = DRIVER

    override fun supportsAsKeywordForTableAlias(): Boolean = false

    override fun supportsExceptSetOperation(): Boolean = false

    override fun supportsModFunction(): Boolean = true

    override fun getSequenceSql(sequenceName: String): String {
        return "select $sequenceName.nextval from dual"
    }

    override fun getSubstringFunction(): String {
        return "substr"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return OracleSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        if (entities.size == 1) {
            return super.getEntityInsertStatementBuilder(context, entities)
        }
        return OracleEntityInsertStatementBuilder(this, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return OracleEntityUpsertStatementBuilder(this, context, entities)
    }
}
