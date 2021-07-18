package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlMultipleColumnsQuery(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val expressions: List<ColumnExpression<*, *>>
) : FlowableSubquery<Columns> {

    override val subqueryContext: SubqueryContext<Columns> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Columns> =
        FlowableSubquerySupport(subqueryContext) { SqlMultipleColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleColumnsQuery(context, options, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Columns>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleColumnsQuery(context, options, expressions, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<Columns> = object : FlowQuery<Columns> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleColumnsQuery(context, options, expressions)
        }
    }

    override fun except(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return support.unionAll(other)
    }
}
