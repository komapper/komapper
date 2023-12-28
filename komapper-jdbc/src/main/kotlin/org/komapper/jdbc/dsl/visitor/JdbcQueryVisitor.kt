package org.komapper.jdbc.dsl.visitor

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
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.jdbc.dsl.runner.JdbcEntityDeleteBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityDeleteSingleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityDeleteSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertMultipleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertMultipleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertSingleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityStoreRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpdateBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpdateSingleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpdateSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertMultipleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertMultipleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleIgnoreRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleUpdateRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationDeleteReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationDeleteRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationInsertSelectRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationInsertValuesReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationInsertValuesRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationUpdateReturningRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationUpdateRunner
import org.komapper.jdbc.dsl.runner.JdbcResultSetTransformers
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.runner.JdbcSchemaCreateRunner
import org.komapper.jdbc.dsl.runner.JdbcSchemaDropRunner
import org.komapper.jdbc.dsl.runner.JdbcScriptExecuteRunner
import org.komapper.jdbc.dsl.runner.JdbcSelectRunner
import org.komapper.jdbc.dsl.runner.JdbcSetOperationRunner
import org.komapper.jdbc.dsl.runner.JdbcTemplateEntityProjectionSelectRunner
import org.komapper.jdbc.dsl.runner.JdbcTemplateExecuteRunner
import org.komapper.jdbc.dsl.runner.JdbcTemplateSelectRunner

object JdbcQueryVisitor : QueryVisitor<JdbcRunner<*>> {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): JdbcRunner<S> {
        val leftRunner = left.accept(this) as JdbcRunner<T>
        val rightRunner = right.accept(this) as JdbcRunner<S>
        return JdbcRunner.AndThen(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): JdbcRunner<S> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.Map(runner, transform)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> zipQuery(left: Query<T>, right: Query<S>): JdbcRunner<Pair<T, S>> {
        val leftRunner = left.accept(this) as JdbcRunner<T>
        val rightRunner = right.accept(this) as JdbcRunner<S>
        return JdbcRunner.Zip(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): JdbcRunner<S> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.FlatMap(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): JdbcRunner<Pair<T, S>> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.FlatZip(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcRunner<S>
        }
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityStoreQuery(
        context: SelectContext<ENTITY, ID, META>,
    ): JdbcRunner<EntityStore> {
        return JdbcEntityStoreRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): JdbcRunner<R> {
        val transformer = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcSelectRunner(context, transformer, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<Unit> {
        return JdbcEntityDeleteBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<Unit> {
        return JdbcEntityDeleteSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityDeleteSingleReturningQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY?> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityDeleteSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityDeleteSingleReturningSingleColumnQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<A?> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityDeleteSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityDeleteSingleReturningPairColumnsQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<Pair<A?, B?>?> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityDeleteSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityDeleteSingleReturningTripleColumnsQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<Triple<A?, B?, C?>?> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityDeleteSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<ENTITY>> {
        return JdbcEntityInsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<ENTITY>> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertMultipleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<List<A?>> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertMultipleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<List<Pair<A?, B?>>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertMultipleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityInsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<ENTITY>> {
        return JdbcEntityInsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY> {
        return JdbcEntityInsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertSingleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<A?> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertSingleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<Pair<A?, B?>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertSingleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<Triple<A?, B?, C?>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityInsertSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<ENTITY>> {
        return JdbcEntityUpdateBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY> {
        return JdbcEntityUpdateSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpdateSingleReturningQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY?> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpdateSingleReturningSingleColumnQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<A?> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpdateSingleReturningPairColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<Pair<A?, B?>?> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpdateSingleReturningTripleColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<Triple<A?, B?, C?>?> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityUpdateSingleReturningRunner(context, entity, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<Long>> {
        return JdbcEntityUpsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<Long> {
        return JdbcEntityUpsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertMultipleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): JdbcRunner<List<ENTITY>> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpsertMultipleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<List<A?>> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpsertMultipleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<List<Pair<A?, B?>>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpsertMultipleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityUpsertMultipleReturningRunner(context, entities, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<Long> {
        return JdbcEntityUpsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entityUpsertSingleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        collect: suspend (Flow<ENTITY>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, R> entityUpsertSingleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, R> entityUpsertSingleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any, R> entityUpsertSingleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcEntityUpsertSingleReturningRunner(context, entity, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY> {
        return JdbcEntityUpsertSingleUpdateRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleIgnoreQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<ENTITY?> {
        return JdbcEntityUpsertSingleIgnoreRunner(context, entity)
    }

    override fun schemaCreateQuery(
        context: SchemaContext,
    ): JdbcRunner<Unit> {
        return JdbcSchemaCreateRunner(context)
    }

    override fun schemaDropQuery(
        context: SchemaContext,
    ): JdbcRunner<Unit> {
        return JdbcSchemaDropRunner(context)
    }

    override fun scriptExecuteQuery(
        context: ScriptContext,
    ): JdbcRunner<Unit> {
        return JdbcScriptExecuteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleNotNullColumn(expression)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleNotNullColumn(expression)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairNotNullColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairNotNullColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleNotNullColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleNotNullColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, R> entityProjectionSelectQuery(
        context: SelectContext<*, *, *>,
        metamodel: EntityMetamodel<ENTITY, *, *>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): JdbcRunner<*> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, R> entityProjectionSetOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, *, *>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): JdbcRunner<*> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): JdbcRunner<Long> {
        return JdbcRelationDeleteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteReturningQuery(context: RelationDeleteContext<ENTITY, ID, META>): JdbcRunner<List<ENTITY>> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcRelationDeleteReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationDeleteReturningSingleColumnQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<List<A?>> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcRelationDeleteReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationDeleteReturningPairColumnsQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<List<Pair<A?, B?>>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcRelationDeleteReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationDeleteReturningTripleColumnsQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcRelationDeleteReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): JdbcRunner<Pair<Long, ID?>> {
        return JdbcRelationInsertValuesRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesReturningQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): JdbcRunner<ENTITY> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcRelationInsertValuesReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationInsertValuesReturningSingleColumnQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<A?> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcRelationInsertValuesReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationInsertValuesReturningPairColumnsQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<Pair<A?, B?>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcRelationInsertValuesReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationInsertValuesReturningTripleColumnsQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<Triple<A?, B?, C?>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcRelationInsertValuesReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>,
    ): JdbcRunner<Pair<Long, List<ID>>> {
        return JdbcRelationInsertSelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): JdbcRunner<Long> {
        return JdbcRelationUpdateRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateReturningQuery(context: RelationUpdateContext<ENTITY, ID, META>): JdbcRunner<List<ENTITY>> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcRelationUpdateReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationUpdateReturningSingleColumnQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): JdbcRunner<List<A?>> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcRelationUpdateReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationUpdateReturningPairColumnsQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): JdbcRunner<List<Pair<A?, B?>>> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcRelationUpdateReturningRunner(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationUpdateReturningTripleColumnsQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): JdbcRunner<List<Triple<A?, B?, C?>>> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcRelationUpdateReturningRunner(context, transform)
    }

    override fun templateExecuteQuery(
        context: TemplateExecuteContext,
    ): JdbcRunner<Long> {
        return JdbcTemplateExecuteRunner(context)
    }

    override fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R,
    ): JdbcRunner<R> {
        return JdbcTemplateSelectRunner(context, transform, collect)
    }

    override fun <T : Any, R> templateEntityProjectionSelectQuery(
        context: TemplateSelectContext,
        metamodel: EntityMetamodel<T, *, *>,
        strategy: ProjectionType,
        collect: suspend (Flow<T>) -> R,
    ): JdbcRunner<*> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel, strategy)
        return JdbcTemplateEntityProjectionSelectRunner(context, transform, collect)
    }
}
