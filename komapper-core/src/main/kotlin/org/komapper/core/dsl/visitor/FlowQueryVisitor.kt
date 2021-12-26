package org.komapper.core.dsl.visitor

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Row

@ThreadSafe
interface FlowQueryVisitor<VISIT_RESULT> {

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, ID, META>,
    ): VISIT_RESULT

    fun <A : Any> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any>
    tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): VISIT_RESULT

    fun multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>
    ): VISIT_RESULT

    fun <T> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T
    ): VISIT_RESULT
}
