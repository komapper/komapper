package org.komapper.core.dsl.visitor

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row

@ThreadSafe
interface FlowQueryVisitor<VISIT_RESULT> {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions = SqlSetOperationOptions.default,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any>
    sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun <T> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions
    ): VISIT_RESULT
}
