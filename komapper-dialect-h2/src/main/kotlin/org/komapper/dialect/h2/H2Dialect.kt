package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityMergeStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
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

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityDeleteStatementBuilder<ENTITY, ID, META> {
        return H2EntityDeleteStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return H2EntityInsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        val insertStatementBuilder = H2EntityInsertStatementBuilder(dialect, context.insertContext, entities)
        val upsertStatementBuilder = H2EntityUpsertStatementBuilder(dialect, context, entities)
        return EntityMergeStatementBuilder(dialect, context, insertStatementBuilder, upsertStatementBuilder)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): RelationDeleteStatementBuilder<ENTITY, ID, META> {
        return H2RelationDeleteStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return H2RelationInsertValuesStatementBuilder(dialect, context)
    }

    override fun supportsConflictTargetInUpsertStatement(): Boolean = false

    override fun supportsDeleteReturning(): Boolean = true

    override fun supportsInsertMultipleReturning(): Boolean = true

    override fun supportsInsertSingleReturning(): Boolean = true

    override fun supportsSearchConditionInUpsertStatement(): Boolean = true

    override fun supportsUpsertMultipleReturning(): Boolean = true

    override fun supportsUpsertSingleReturning(): Boolean = true
}
