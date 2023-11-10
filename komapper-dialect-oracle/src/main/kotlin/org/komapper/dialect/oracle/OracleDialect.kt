package org.komapper.dialect.oracle

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

interface OracleDialect : Dialect {

    companion object : Dialect.Identifier {
        private const val DRIVER = "oracle"

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 1
        const val TABLE_NOT_EXISTS_ERROR_CODE = 942
        const val NAME_ALREADY_USED_ERROR_CODE = 955
        const val SEQUENCE_NOT_EXISTS_ERROR_CODE = 2289
        override val driver: String = DRIVER
    }

    override val driver: String get() = DRIVER

    override fun getLocateFunctionType(): LocateFunctionType {
        return LocateFunctionType.INSTR
    }

    override fun getRandomFunction(): String = "dbms_random.value"

    override fun getSequenceSql(sequenceName: String): String {
        return "select $sequenceName.nextval from dual"
    }

    override fun getSubstringFunction(): String {
        return "substr"
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        return OracleSchemaStatementBuilder(dialect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityDeleteStatementBuilder<ENTITY, ID, META> {
        return OracleEntityDeleteStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityInsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityInsertStatementBuilder<ENTITY, ID, META> {
        return OracleEntityInsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): EntityUpdateStatementBuilder<ENTITY, ID, META> {
        return OracleEntityUpdateStatementBuilder(dialect, context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        return OracleEntityUpsertStatementBuilder(dialect, context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationDeleteStatementBuilder(
        dialect: BuilderDialect,
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): RelationDeleteStatementBuilder<ENTITY, ID, META> {
        return OracleRelationDeleteStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationInsertValuesStatementBuilder(
        dialect: BuilderDialect,
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
        return OracleRelationInsertValuesStatementBuilder(dialect, context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getRelationUpdateStatementBuilder(
        dialect: BuilderDialect,
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): RelationUpdateStatementBuilder<ENTITY, ID, META> {
        return OracleRelationUpdateStatementBuilder(dialect, context)
    }

    override fun supportsAsKeywordForTableAlias(): Boolean = false

    override fun supportsAutoIncrementWhenInsertingMultipleRows(): Boolean = false

    override fun supportsConflictTargetInUpsertStatement(): Boolean = false

    override fun supportsCreateIfNotExists(): Boolean = false

    override fun supportsDropIfExists(): Boolean = false

    override fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = false

    override fun supportsLockOfColumns(): Boolean = true

    override fun supportsLockOptionNowait(): Boolean = true

    override fun supportsLockOptionSkipLocked(): Boolean = true

    override fun supportsLockOptionWait(): Boolean = true

    override fun supportsModuloOperator(): Boolean = false

    override fun supportsRecursiveKeywordInCommonTableExpression(): Boolean = false

    override fun supportsSelectStatementWithoutFromClause(): Boolean = false

    override fun supportsSetOperationExcept(): Boolean = false

    override fun supportsSetOperationMinus(): Boolean = true

    override fun supportsSearchConditionInUpsertStatement(): Boolean = true
}
