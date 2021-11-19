package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlMultipleColumnsQuery(
    override val context: SelectContext<*, *, *>,
    private val options: SelectOptions,
    private val expressions: List<ColumnExpression<*, *>>
) : FlowSubquery<Columns> {

    private val support: FlowSubquerySupport<Columns> =
        FlowSubquerySupport(context) { SqlMultipleColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleColumnsQuery(context, options, expressions) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleColumnsQuery(context, options, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Columns>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleColumnsQuery(context, options, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Columns>): FlowSetOperationQuery<Columns> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Columns>): FlowSetOperationQuery<Columns> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Columns>): FlowSetOperationQuery<Columns> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Columns>): FlowSetOperationQuery<Columns> {
        return support.unionAll(other)
    }
}
