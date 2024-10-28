package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.LocateFunctionType
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.builder.RelationUpdateStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface PostgreSqlDialect : Dialect {
    companion object : Dialect.Identifier {
        private const val DRIVER = "postgresql"

        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER

    override fun getLocateFunctionType(): LocateFunctionType {
        return LocateFunctionType.POSITION
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return PostgreSqlSchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityDeleteStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlEntityDeleteStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlEntityInsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityUpdateStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlEntityUpdateStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return PostgreSqlEntityUpsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): RelationDeleteStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlRelationDeleteStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlRelationInsertValuesStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): RelationUpdateStatementBuilder<ENTITY, ID, META> {
        return PostgreSqlRelationUpdateStatementBuilder(dialect, context)
    }

    override fun supportsConflictTargetInUpsertStatement(): Boolean = true

    override fun supportsIndexPredicateInUpsertStatement(): Boolean = true

    override fun supportsDeleteReturning(): Boolean = true

    override fun supportsLockOfTables(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsInsertMultipleReturning(): Boolean = true

    override fun supportsInsertSingleReturning(): Boolean = true

    override fun supportsSearchConditionInUpsertStatement(): Boolean = true

    override fun supportsUpdateReturning(): Boolean = true

    override fun supportsUpsertMultipleReturning(): Boolean = true

    override fun supportsUpsertSingleReturning(): Boolean = true
}
