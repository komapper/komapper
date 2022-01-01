package org.komapper.r2dbc.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.EntityDeleteSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityStoreR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpdateSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertMultipleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleIgnoreR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleR2dbcRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleUpdateR2dbcRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationDeleteR2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationInsertSelectR2dbcRunner
import org.komapper.r2dbc.dsl.runner.RelationInsertValuesR2dbcRunner
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

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityStoreQuery(
        context: SelectContext<ENTITY, ID, META>
    ): R2dbcRunner<*> {
        return EntityStoreR2dbcRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY
    ): R2dbcRunner<Unit> {
        return EntityDeleteSingleR2dbcRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<List<ENTITY>> {
        return EntityInsertMultipleR2dbcRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityInsertSingleR2dbcRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityUpdateSingleR2dbcRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<*> {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): R2dbcRunner<Int> {
        return EntityUpsertMultipleR2dbcRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<Int> {
        return EntityUpsertSingleR2dbcRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): R2dbcRunner<ENTITY> {
        return EntityUpsertSingleUpdateR2dbcRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleIgnoreQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): R2dbcRunner<*> {
        return EntityUpsertSingleIgnoreR2dbcRunner(context, entity)
    }

    override fun schemaCreateQuery(
        context: SchemaContext
    ): R2dbcRunner<Unit> {
        return SchemaCreateR2dbcRunner(context)
    }

    override fun schemaDropQuery(
        context: SchemaContext
    ): R2dbcRunner<Unit> {
        return SchemaDropR2dbcRunner(context)
    }

    override fun schemaDropAllQuery(context: SchemaContext): R2dbcRunner<Unit> {
        return SchemaDropAllR2dbcRunner(context)
    }

    override fun scriptExecuteQuery(
        context: ScriptContext
    ): R2dbcRunner<Unit> {
        return ScriptExecuteR2dbcRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.singleEntity(metamodel)
        return SetOperationR2dbcRunner(context, provide, collect)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): R2dbcRunner<*> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleColumns(expressions)
        return SelectR2dbcRunner(context, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return SelectR2dbcRunner(context, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SelectR2dbcRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SetOperationR2dbcRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>
    ): R2dbcRunner<Int> {
        return RelationDeleteR2dbcRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>
    ): R2dbcRunner<Pair<Int, ID?>> {
        return RelationInsertValuesR2dbcRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>
    ): R2dbcRunner<Pair<Int, List<ID>>> {
        return RelationInsertSelectR2dbcRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>
    ): R2dbcRunner<*> {
        return RelationUpdateR2dbcRunner(context)
    }

    override fun templateExecuteQuery(
        context: TemplateExecuteContext
    ): R2dbcRunner<Int> {
        return TemplateExecuteR2dbcRunner(context)
    }

    override fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R
    ): R2dbcRunner<R> {
        return TemplateSelectR2dbcRunner(context, transform, collect)
    }
}
