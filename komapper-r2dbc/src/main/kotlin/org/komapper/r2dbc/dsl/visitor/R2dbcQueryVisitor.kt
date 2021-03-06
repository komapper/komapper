package org.komapper.r2dbc.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteBatchOptions
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.options.EntityInsertBatchOptions
import org.komapper.core.dsl.options.EntityInsertOptions
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.options.EntityUpdateBatchOptions
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaCreateOptions
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.core.dsl.options.SqlDeleteOptions
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.R2dbcEntityDeleteSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntitySelectQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpdateSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcSchemaCreateQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSchemaDropAllQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSchemaDropQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcScriptExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSqlDeleteQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSqlInsertQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSqlSelectQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSqlSetOperationQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSqlUpdateQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateSelectQueryRunner

internal object R2dbcQueryVisitor : QueryVisitor<R2dbcQueryRunner<*>> {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> plusQuery(left: Query<T>, right: Query<S>): R2dbcQueryRunner<S> {
        val leftRunner = left.accept(this) as R2dbcQueryRunner<T>
        val rightRunner = right.accept(this) as R2dbcQueryRunner<S>
        return R2dbcQueryRunner.Plus(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): R2dbcQueryRunner<S> {
        val runner = query.accept(this) as R2dbcQueryRunner<T>
        return R2dbcQueryRunner.FlatMap(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): R2dbcQueryRunner<Pair<T, S>> {
        val runner = query.accept(this) as R2dbcQueryRunner<T>
        return R2dbcQueryRunner.FlatZip(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S>
        }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: EntitySelectOptions,
        transform: suspend (Flow<ENTITY>) -> R
    ): R2dbcQueryRunner<R> {
        return R2dbcEntitySelectQueryRunner(context, options, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteBatchOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<*> {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteOptions,
        entity: ENTITY
    ): R2dbcQueryRunner<Unit> {
        return R2dbcEntityDeleteSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<List<ENTITY>> {
        return R2dbcEntityInsertMultipleQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertBatchOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<*> {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entity: ENTITY
    ): R2dbcQueryRunner<ENTITY> {
        return R2dbcEntityInsertSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateBatchOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<*> {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateOptions,
        entity: ENTITY
    ): R2dbcQueryRunner<ENTITY> {
        return R2dbcEntityUpdateSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<*> {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcQueryRunner<Int> {
        return R2dbcEntityUpsertMultipleQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): R2dbcQueryRunner<Int> {
        return R2dbcEntityUpsertSingleQueryRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaCreateOptions
    ): R2dbcQueryRunner<Unit> {
        return R2dbcSchemaCreateQueryRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaDropOptions
    ): R2dbcQueryRunner<Unit> {
        return R2dbcSchemaDropQueryRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaDropAllOptions): R2dbcQueryRunner<Unit> {
        return R2dbcSchemaDropAllQueryRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptExecuteOptions
    ): R2dbcQueryRunner<Unit> {
        return R2dbcScriptExecuteQueryRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): R2dbcQueryRunner<R> {
        val provide = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcSqlSetOperationQueryRunner(context, options, provide, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        options: SqlSetOperationOptions,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        options: SqlSetOperationOptions,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        options: SqlSetOperationOptions,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcQueryRunner<R> {
        val provide = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSqlSelectQueryRunner(context, options, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcQueryRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        options: SqlDeleteOptions
    ): R2dbcQueryRunner<Int> {
        return R2dbcSqlDeleteQueryRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        options: SqlInsertOptions
    ): R2dbcQueryRunner<Pair<Int, Long?>> {
        return R2dbcSqlInsertQueryRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        options: SqlUpdateOptions
    ): R2dbcQueryRunner<Int> {
        return R2dbcSqlUpdateQueryRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): R2dbcQueryRunner<Int> {
        return R2dbcTemplateExecuteQueryRunner(sql, data, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): R2dbcQueryRunner<R> {
        return R2dbcTemplateSelectQueryRunner(sql, data, transform, options, collect)
    }
}
