package org.komapper.core.dsl.visitor

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.runner.FlowQueryRunner

@ThreadSafe
interface FlowQueryVisitor {

    fun <T : Any> sqlSelectQuery(
        context: SqlSelectContext<T, *, *>,
        option: SqlSelectOption
    ): FlowQueryRunner

    fun <T : Any> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        option: SqlSetOperationOption,
        metamodel: EntityMetamodel<T, *, *>,
    ): FlowQueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>
    sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Pair<A_META, B_META>,
    ): FlowQueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>
    sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        option: SqlSetOperationOption,
        metamodels: Pair<A_META, B_META>,
    ): FlowQueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>>
    sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Triple<A_META, B_META, C_META>,
    ): FlowQueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>>
    sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        option: SqlSetOperationOption,
        metamodels: Triple<A_META, B_META, C_META>,
    ): FlowQueryRunner

    fun sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
    ): FlowQueryRunner

    fun sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        option: SqlSetOperationOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
    ): FlowQueryRunner

    fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expression: ColumnExpression<A, *>,
    ): FlowQueryRunner

    fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        option: SqlSetOperationOption,
        expression: ColumnExpression<A, *>,
    ): FlowQueryRunner

    fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): FlowQueryRunner

    fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        option: SqlSetOperationOption = SqlSetOperationOption.default,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): FlowQueryRunner

    fun <A : Any, B : Any, C : Any>
    sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): FlowQueryRunner

    fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        option: SqlSetOperationOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): FlowQueryRunner

    fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner

    fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        option: SqlSetOperationOption,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner
}
