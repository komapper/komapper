package org.komapper.dialect.postgresql

import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface PostgreSqlDialect : Dialect {

    companion object : Dialect.Identifier {
        private const val DRIVER = "postgresql"
        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return PostgreSqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return PostgreSqlEntityUpsertStatementBuilder(this, context, entities)
    }

    override fun supportsLockOfTables(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true
}
