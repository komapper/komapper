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
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.R2dbcEntityDeleteBatchRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityDeleteSingleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertBatchRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertMultipleReturningRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertMultipleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertSingleReturningRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityInsertSingleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityStoreRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpdateBatchRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpdateSingleReturningRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpdateSingleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertBatchRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertMultipleReturningRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertMultipleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertSingleIgnoreRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertSingleReturningRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertSingleRunner
import org.komapper.r2dbc.dsl.runner.R2dbcEntityUpsertSingleUpdateRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRelationDeleteRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRelationInsertSelectRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRelationInsertValuesRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRelationUpdateRunner
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSchemaCreateRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSchemaDropRunner
import org.komapper.r2dbc.dsl.runner.R2dbcScriptExecuteRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSelectRunner
import org.komapper.r2dbc.dsl.runner.R2dbcSetOperationRunner
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateExecuteRunner
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateSelectRunner

object R2dbcQueryVisitor : QueryVisitor<R2dbcRunner<*>> {

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
        context: SelectContext<ENTITY, ID, META>,
    ): R2dbcRunner<EntityStore> {
        return R2dbcEntityStoreRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<Unit> {
        return R2dbcEntityDeleteBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<Unit> {
        return R2dbcEntityDeleteSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<ENTITY>> {
        return R2dbcEntityInsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<ENTITY>> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertMultipleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): R2dbcRunner<List<A?>> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertMultipleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcRunner<List<Pair<A?, B?>>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertMultipleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<ENTITY>> {
        return R2dbcEntityInsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY> {
        return R2dbcEntityInsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertSingleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): R2dbcRunner<*> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertSingleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcRunner<*> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertSingleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcRunner<*> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<ENTITY>> {
        return R2dbcEntityUpdateBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY> {
        return R2dbcEntityUpdateSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpdateSingleReturningQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY?> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpdateSingleReturningSingleColumnQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): R2dbcRunner<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpdateSingleReturningPairColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcRunner<Pair<A?, B?>?> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpdateSingleReturningTripleColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcRunner<Triple<A?, B?, C?>?> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<Long>> {
        return R2dbcEntityUpsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<Long> {
        return R2dbcEntityUpsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertMultipleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): R2dbcRunner<List<ENTITY>> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpsertMultipleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): R2dbcRunner<List<A?>> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpsertMultipleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcRunner<List<Pair<A?, B?>>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpsertMultipleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<Long> {
        return R2dbcEntityUpsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entityUpsertSingleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        collect: suspend (Flow<ENTITY>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, R> entityUpsertSingleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, R> entityUpsertSingleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any, R> entityUpsertSingleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY> {
        return R2dbcEntityUpsertSingleUpdateRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleIgnoreQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): R2dbcRunner<ENTITY?> {
        return R2dbcEntityUpsertSingleIgnoreRunner(context, entity)
    }

    override fun schemaCreateQuery(
        context: SchemaContext,
    ): R2dbcRunner<Unit> {
        return R2dbcSchemaCreateRunner(context)
    }

    override fun schemaDropQuery(
        context: SchemaContext,
    ): R2dbcRunner<Unit> {
        return R2dbcSchemaDropRunner(context)
    }

    override fun scriptExecuteQuery(
        context: ScriptContext,
    ): R2dbcRunner<Unit> {
        return R2dbcScriptExecuteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSelectRunner(context, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): R2dbcRunner<R> {
        val provide = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return R2dbcSelectRunner(context, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSelectRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): R2dbcRunner<R> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSetOperationRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): R2dbcRunner<Long> {
        return R2dbcRelationDeleteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): R2dbcRunner<Pair<Long, ID?>> {
        return R2dbcRelationInsertValuesRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>,
    ): R2dbcRunner<Pair<Long, List<ID>>> {
        return R2dbcRelationInsertSelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): R2dbcRunner<Long> {
        return R2dbcRelationUpdateRunner(context)
    }

    override fun templateExecuteQuery(
        context: TemplateExecuteContext,
    ): R2dbcRunner<Long> {
        return R2dbcTemplateExecuteRunner(context)
    }

    override fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R,
    ): R2dbcRunner<R> {
        return R2dbcTemplateSelectRunner(context, transform, collect)
    }
}
