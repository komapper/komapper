package org.komapper.r2dbc.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.EntityDeleteSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityStoreR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpdateSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationDeleteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationInsertR2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationUpdateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaCreateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropAllR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropR2dbcRunner
import org.komapper.r2dbc.dsl.runner.ScriptExecuteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SelectR2dbcRunner
import org.komapper.r2dbc.dsl.runner.SetOperationR2dbcRunner
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

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityContextQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): R2dbcRunner<*> {
        return EntityStoreR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entity: ENTITY
    ): R2dbcRunner<Unit> {
        return EntityDeleteSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<List<ENTITY>> {
        return EntityInsertMultipleR2dbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityInsertSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityUpdateSingleR2dbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): R2dbcRunner<Int> {
        return EntityUpsertMultipleR2dbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): R2dbcRunner<Int> {
        return EntityUpsertSingleR2dbcRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): R2dbcRunner<Unit> {
        return SchemaCreateR2dbcRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): R2dbcRunner<Unit> {
        return SchemaDropR2dbcRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaOptions): R2dbcRunner<Unit> {
        return SchemaDropAllR2dbcRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptOptions
    ): R2dbcRunner<Unit> {
        return ScriptExecuteR2dbcRunner(sql, options)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.singleEntity(metamodel)
        return SetOperationR2dbcRunner(context, options, provide, collect)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleColumns(expressions)
        return SelectR2dbcRunner(context, options, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SelectR2dbcRunner(context, options, transform, collect)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SetOperationR2dbcRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions
    ): R2dbcRunner<Int> {
        return RelationDeleteR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertQuery(
        context: RelationInsertContext<ENTITY, ID, META>,
        options: InsertOptions
    ): R2dbcRunner<Pair<Int, ID?>> {
        return RelationInsertR2dbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions
    ): R2dbcRunner<*> {
        return RelationUpdateR2dbcRunner(context, options)
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
