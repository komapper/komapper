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
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row

@ThreadSafe
interface QueryVisitor<VISIT_RESULT> {
    fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): VISIT_RESULT

    fun <T, S> zipQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityStoreQuery(
        context: SelectContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityDeleteSingleReturningQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityDeleteSingleReturningSingleColumnQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityDeleteSingleReturningPairColumnsQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityDeleteSingleReturningTripleColumnsQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertMultipleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertMultipleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertMultipleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleReturningQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityInsertSingleReturningSingleColumnQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityInsertSingleReturningPairColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityInsertSingleReturningTripleColumnsQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpdateSingleReturningQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpdateSingleReturningSingleColumnQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpdateSingleReturningPairColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpdateSingleReturningTripleColumnsQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertMultipleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> entityUpsertMultipleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> entityUpsertMultipleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> entityUpsertMultipleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> entityUpsertSingleReturningQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        collect: suspend (Flow<ENTITY>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, R> entityUpsertSingleReturningSingleColumnQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, R> entityUpsertSingleReturningPairColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any, R> entityUpsertSingleReturningTripleColumnsQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityUpsertSingleIgnoreQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun schemaCreateQuery(
        context: SchemaContext,
    ): VISIT_RESULT

    fun schemaDropQuery(
        context: SchemaContext,
    ): VISIT_RESULT

    fun scriptExecuteQuery(
        context: ScriptContext,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): VISIT_RESULT

    fun <T : Any, R> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R,
    ): VISIT_RESULT

    fun <A : Any, R> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): VISIT_RESULT

    fun <A : Any, R> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): VISIT_RESULT

    fun <A : Any, R> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R,
    ): VISIT_RESULT

    fun <A : Any, R> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, R> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, R> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, R> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, R> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R,
    ): VISIT_RESULT

    fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): VISIT_RESULT

    fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, R> entityProjectionSelectQuery(
        context: SelectContext<*, *, *>,
        metamodel: EntityMetamodel<ENTITY, *, *>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, R> entityProjectionSetOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, *, *>,
        collect: suspend (Flow<ENTITY>) -> R,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationDeleteReturningQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationDeleteReturningSingleColumnQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationDeleteReturningPairColumnsQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationDeleteReturningTripleColumnsQuery(
        context: RelationDeleteContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertValuesReturningQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationInsertValuesReturningSingleColumnQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationInsertValuesReturningPairColumnsQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationInsertValuesReturningTripleColumnsQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationUpdateReturningQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any> relationUpdateReturningSingleColumnQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any> relationUpdateReturningPairColumnsQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any> relationUpdateReturningTripleColumnsQuery(
        context: RelationUpdateContext<ENTITY, ID, META>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun templateExecuteQuery(
        context: TemplateExecuteContext,
    ): VISIT_RESULT

    fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R,
    ): VISIT_RESULT

    fun <T : Any, R> templateEntityProjectionSelectQuery(
        context: TemplateSelectContext,
        metamodel: EntityMetamodel<T, *, *>,
        strategy: ProjectionType,
        collect: suspend (Flow<T>) -> R,
    ): VISIT_RESULT
}
