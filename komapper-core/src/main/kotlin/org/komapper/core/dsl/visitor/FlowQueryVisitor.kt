package org.komapper.core.dsl.visitor

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row

@ThreadSafe
interface FlowQueryVisitor<VISIT_RESULT> {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> selectQuery(
        context: SelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): VISIT_RESULT

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> setOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        metamodel: EntityMetamodel<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <A : Any> singleColumnQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any> singleColumnSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> pairColumnsQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions = SelectOptions.default,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any>
    tripleColumnsQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun multipleColumnsQuery(
        context: SelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun <T> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions
    ): VISIT_RESULT
}
