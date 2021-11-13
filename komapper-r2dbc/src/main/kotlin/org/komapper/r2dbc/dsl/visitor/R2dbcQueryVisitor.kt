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
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.EntityAggregateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityDeleteSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntitySelectR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpdateSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaCreateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropAllR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropR2dbcRunner
import org.komapper.r2dbc.dsl.runner.ScriptExecuteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SqlDeleteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SqlInsertR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SqlSelectR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SqlSetOperationR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SqlUpdateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.TemplateExecuteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.TemplateSelectR2dbcRunner

internal object R2dbcQueryVisitor : QueryVisitor<R2dbcRunner<*>> {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): R2dbcRunner<S> {
        val leftRunner = left.accept(this) as R2dbcRunner<T>
        val rightRunner = right.accept(this) as R2dbcRunner<S>
        return R2dbcRunner.AndThen(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): R2dbcRunner<S> {
        val runner = query.accept(this) as R2dbcRunner<T>
        return R2dbcRunner.Map(runner, transform)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> zipQuery(left: Query<T>, right: Query<S>): R2dbcRunner<Pair<T, S>> {
        val leftRunner = left.accept(this) as R2dbcRunner<T>
        val rightRunner = right.accept(this) as R2dbcRunner<S>
        return R2dbcRunner.Zip(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): R2dbcRunner<S> {
        val runner = query.accept(this) as R2dbcRunner<T>
        return R2dbcRunner.FlatMap(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): R2dbcRunner<Pair<T, S>> {
        val runner = query.accept(this) as R2dbcRunner<T>
        return R2dbcRunner.FlatZip(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcRunner<S>
        }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityAggregateQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: EntitySelectOptions
    ): R2dbcRunner<*> {
        return EntityAggregateR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: EntitySelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return EntitySelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteBatchOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteOptions,
        entity: ENTITY
    ): R2dbcRunner<Unit> {
        return EntityDeleteSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<List<ENTITY>> {
        return EntityInsertMultipleR2dbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertBatchOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityInsertSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateBatchOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateOptions,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityUpdateSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<Int> {
        return EntityUpsertMultipleR2dbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): R2dbcRunner<Int> {
        return EntityUpsertSingleR2dbcRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaCreateOptions
    ): R2dbcRunner<Unit> {
        return SchemaCreateR2dbcRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaDropOptions
    ): R2dbcRunner<Unit> {
        return SchemaDropR2dbcRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaDropAllOptions): R2dbcRunner<Unit> {
        return SchemaDropAllR2dbcRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptExecuteOptions
    ): R2dbcRunner<Unit> {
        return ScriptExecuteR2dbcRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SqlSelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.singleEntity(metamodel)
        return SqlSetOperationR2dbcRunner(context, options, provide, collect)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSelectR2dbcRunner(context, options, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        options: SqlDeleteOptions
    ): R2dbcRunner<Int> {
        return SqlDeleteR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        options: SqlInsertOptions
    ): R2dbcRunner<Pair<Int, ID?>> {
        return SqlInsertR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        options: SqlUpdateOptions
    ): R2dbcRunner<Int> {
        return SqlUpdateR2dbcRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): R2dbcRunner<Int> {
        return TemplateExecuteR2dbcRunner(sql, data, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): R2dbcRunner<R> {
        return TemplateSelectR2dbcRunner(sql, data, transform, options, collect)
    }
}
