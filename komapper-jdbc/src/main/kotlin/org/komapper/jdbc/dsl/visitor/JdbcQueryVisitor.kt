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
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.jdbc.dsl.runner.JdbcEntityDeleteBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityDeleteSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertMultipleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityInsertSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityStoreRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpdateBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpdateSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertBatchRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertMultipleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleIgnoreRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleRunner
import org.komapper.jdbc.dsl.runner.JdbcEntityUpsertSingleUpdateRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationDeleteRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationInsertSelectRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationInsertValuesRunner
import org.komapper.jdbc.dsl.runner.JdbcRelationUpdateRunner
import org.komapper.jdbc.dsl.runner.JdbcResultSetTransformers
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.runner.JdbcSchemaCreateRunner
import org.komapper.jdbc.dsl.runner.JdbcSchemaDropRunner
import org.komapper.jdbc.dsl.runner.JdbcScriptExecuteRunner
import org.komapper.jdbc.dsl.runner.JdbcSelectRunner
import org.komapper.jdbc.dsl.runner.JdbcSetOperationRunner
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
        context: SelectContext<ENTITY, ID, META>
    ): JdbcRunner<*> {
        return JdbcEntityStoreRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): JdbcRunner<R> {
        val transformer = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcSelectRunner(context, transformer, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<Unit> {
        return JdbcEntityDeleteBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY
    ): JdbcRunner<Unit> {
        return JdbcEntityDeleteSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<List<ENTITY>> {
        return JdbcEntityInsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<List<ENTITY>> {
        return JdbcEntityInsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): JdbcRunner<ENTITY> {
        return JdbcEntityInsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<*> {
        return JdbcEntityUpdateBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY
    ): JdbcRunner<ENTITY> {
        return JdbcEntityUpdateSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<List<Int>> {
        return JdbcEntityUpsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): JdbcRunner<Int> {
        return JdbcEntityUpsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): JdbcRunner<Int> {
        return JdbcEntityUpsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY
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
        context: SchemaContext
    ): JdbcRunner<Unit> {
        return JdbcSchemaCreateRunner(context)
    }

    override fun schemaDropQuery(
        context: SchemaContext
    ): JdbcRunner<Unit> {
        return JdbcSchemaDropRunner(context)
    }

    override fun scriptExecuteQuery(
        context: ScriptContext
    ): JdbcRunner<Unit> {
        return JdbcScriptExecuteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleNotNullColumn(expression)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, R> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): JdbcRunner<*> {
        val transform = JdbcResultSetTransformers.singleNotNullColumn(expression)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairNotNullColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, R> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairNotNullColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleNotNullColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleNotNullColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return JdbcSelectRunner(context, transform, collect)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return JdbcSetOperationRunner(context, transform, collect)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>
    ): JdbcRunner<Int> {
        return JdbcRelationDeleteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>
    ): JdbcRunner<Pair<Int, ID?>> {
        return JdbcRelationInsertValuesRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>
    ): JdbcRunner<Pair<Int, List<ID>>> {
        return JdbcRelationInsertSelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>
    ): JdbcRunner<*> {
        return JdbcRelationUpdateRunner(context)
    }

    override fun templateExecuteQuery(
        context: TemplateExecuteContext
    ): JdbcRunner<Int> {
        return JdbcTemplateExecuteRunner(context)
    }

    override fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R
    ): JdbcRunner<R> {
        return JdbcTemplateSelectRunner(context, transform, collect)
    }
}
