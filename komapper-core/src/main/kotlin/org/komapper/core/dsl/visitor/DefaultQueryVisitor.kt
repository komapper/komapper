package org.komapper.core.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
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
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.core.dsl.runner.EntityDeleteSingleRunner
import org.komapper.core.dsl.runner.EntityInsertBatchRunner
import org.komapper.core.dsl.runner.EntityInsertMultipleRunner
import org.komapper.core.dsl.runner.EntityInsertSingleRunner
import org.komapper.core.dsl.runner.EntityUpdateBatchRunner
import org.komapper.core.dsl.runner.EntityUpdateSingleRunner
import org.komapper.core.dsl.runner.EntityUpsertBatchRunner
import org.komapper.core.dsl.runner.EntityUpsertMultipleRunner
import org.komapper.core.dsl.runner.EntityUpsertSingleRunner
import org.komapper.core.dsl.runner.RelationDeleteRunner
import org.komapper.core.dsl.runner.RelationInsertSelectRunner
import org.komapper.core.dsl.runner.RelationInsertValuesRunner
import org.komapper.core.dsl.runner.RelationUpdateRunner
import org.komapper.core.dsl.runner.Runner
import org.komapper.core.dsl.runner.SchemaCreateRunner
import org.komapper.core.dsl.runner.SchemaDropAllRunner
import org.komapper.core.dsl.runner.SchemaDropRunner
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.core.dsl.runner.TemplateExecuteRunner
import org.komapper.core.dsl.runner.TemplateSelectRunner

@ThreadSafe
internal object DefaultQueryVisitor : QueryVisitor<Runner> {

    override fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): Runner {
        val leftRunner = left.accept(this)
        val rightRunner = right.accept(this)
        return Runner.AndThen(leftRunner, rightRunner)
    }

    override fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): Runner {
        val runner = query.accept(this)
        return Runner.Map(runner)
    }

    override fun <T, S> zipQuery(left: Query<T>, right: Query<S>): Runner {
        val leftRunner = left.accept(this)
        val rightRunner = right.accept(this)
        return Runner.Zip(leftRunner, rightRunner)
    }

    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): Runner {
        val runner = query.accept(this)
        return Runner.FlatMap(runner)
    }

    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): Runner {
        val runner = query.accept(this)
        return Runner.FlatZip(runner)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityStoreQuery(
        context: SelectContext<ENTITY, ID, META>
    ): Runner {
        return SelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityDeleteBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY
    ): Runner {
        return EntityDeleteSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): Runner {
        return EntityInsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpdateBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY
    ): Runner {
        return EntityUpdateSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertBatchRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertMultipleRunner(context, entities)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): Runner {
        return EntityUpsertSingleRunner(context, entity)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertDuplicateKeyIgnoreSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): Runner {
        return EntityUpsertSingleRunner(context, entity)
    }

    override fun schemaCreateQuery(
        context: SchemaContext
    ): Runner {
        return SchemaCreateRunner(context)
    }

    override fun schemaDropQuery(
        context: SchemaContext
    ): Runner {
        return SchemaDropRunner(context)
    }

    override fun schemaDropAllQuery(context: SchemaContext): Runner {
        return SchemaDropAllRunner(context)
    }

    override fun scriptExecuteQuery(
        context: ScriptContext
    ): Runner {
        return ScriptExecuteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return SetOperationRunner(context)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SetOperationRunner(context)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SetOperationRunner(context)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SetOperationRunner(context)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SelectRunner(context)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SetOperationRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>
    ): Runner {
        return RelationDeleteRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>
    ): Runner {
        return RelationInsertValuesRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(context: RelationInsertSelectContext<ENTITY, ID, META>): Runner {
        return RelationInsertSelectRunner(context)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>
    ): Runner {
        return RelationUpdateRunner(context)
    }

    override fun templateExecuteQuery(
        context: TemplateExecuteContext
    ): Runner {
        return TemplateExecuteRunner(context)
    }

    override fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return TemplateSelectRunner(context)
    }
}
