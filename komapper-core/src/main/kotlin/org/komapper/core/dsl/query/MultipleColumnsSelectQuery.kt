package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class MultipleColumnsSelectQuery(
    override val context: SelectContext<*, *, *>,
    private val expressions: List<ColumnExpression<*, *>>,
) : FlowSubquery<Record> {
    private val support: FlowSubquerySupport<Record> =
        FlowSubquerySupport(context) { MultipleColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.multipleColumnsSelectQuery(context, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Record>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.multipleColumnsSelectQuery(context, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Record>): FlowSetOperationQuery<Record> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Record>): FlowSetOperationQuery<Record> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Record>): FlowSetOperationQuery<Record> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Record>): FlowSetOperationQuery<Record> {
        return support.unionAll(other)
    }
}
