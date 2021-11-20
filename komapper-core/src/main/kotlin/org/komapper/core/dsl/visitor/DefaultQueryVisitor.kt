package org.komapper.core.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
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
import org.komapper.core.dsl.runner.RelationInsertRunner
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

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityContextQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityDeleteBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entity: ENTITY
    ): Runner {
        return EntityDeleteSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertMultipleRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY
    ): Runner {
        return EntityInsertSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpdateBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entity: ENTITY
    ): Runner {
        return EntityUpdateSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertMultipleRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): Runner {
        return EntityUpsertSingleRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): Runner {
        return SchemaCreateRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): Runner {
        return SchemaDropRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaOptions): Runner {
        return SchemaDropAllRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptOptions
    ): Runner {
        return ScriptExecuteRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return SetOperationRunner(context, options)
    }

    override fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SetOperationRunner(context, options)
    }

    override fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SetOperationRunner(context, options)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SetOperationRunner(context, options)
    }

    override fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SelectRunner(context, options)
    }

    override fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SetOperationRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions
    ): Runner {
        return RelationDeleteRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> relationInsertQuery(
        context: RelationInsertContext<ENTITY, ID, META>,
        options: InsertOptions
    ): Runner {
        return RelationInsertRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions
    ): Runner {
        return RelationUpdateRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): Runner {
        return TemplateExecuteRunner(sql, data, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return TemplateSelectRunner(sql, data, options)
    }
}
