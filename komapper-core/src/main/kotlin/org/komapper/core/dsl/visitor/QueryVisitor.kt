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

@ThreadSafe
interface QueryVisitor<VISIT_RESULT> {

    fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): VISIT_RESULT

    fun <T, S> zipQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityContextQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): VISIT_RESULT

    fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): VISIT_RESULT

    fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaOptions
    ): VISIT_RESULT

    fun schemaDropAllQuery(options: SchemaOptions): VISIT_RESULT

    fun scriptExecuteQuery(
        sql: String,
        options: ScriptOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): VISIT_RESULT

    fun <T : Any, R>
    setOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleColumnSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions = SelectOptions.default,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): VISIT_RESULT

    fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): VISIT_RESULT

    fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        options: DeleteOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    relationInsertQuery(
        context: RelationInsertContext<ENTITY, ID, META>,
        options: InsertOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        options: UpdateOptions
    ): VISIT_RESULT

    fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): VISIT_RESULT

    fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): VISIT_RESULT
}
