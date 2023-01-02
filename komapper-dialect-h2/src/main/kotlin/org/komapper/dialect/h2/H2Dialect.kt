package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface H2Dialect : Dialect {

    companion object : Dialect.Identifier {
        private const val DRIVER = "h2"

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return H2SchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return H2EntityUpsertStatementBuilder(dialect, context, entities)
    }
}
