package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption

class SqlMultipleColumnsQuery(
    val context: SqlSelectContext<*, *, *>,
    val option: SqlSelectOption,
    val expressions: List<ColumnExpression<*, *>>
) : Subquery<Columns> {

    override fun first(): Query<Columns> {
        TODO("Not yet implemented")
    }

    override fun firstOrNull(): Query<Columns?> {
        TODO("Not yet implemented")
    }

    override fun <R> collect(transform: (Sequence<Columns>) -> R): Query<R> {
        TODO("Not yet implemented")
    }

    override val subqueryContext: SubqueryContext<Columns>
        get() = TODO("Not yet implemented")

    override fun except(other: Subquery<Columns>): SetOperationQuery<Columns> {
        TODO("Not yet implemented")
    }

    override fun intersect(other: Subquery<Columns>): SetOperationQuery<Columns> {
        TODO("Not yet implemented")
    }

    override fun union(other: Subquery<Columns>): SetOperationQuery<Columns> {
        TODO("Not yet implemented")
    }

    override fun unionAll(other: Subquery<Columns>): SetOperationQuery<Columns> {
        TODO("Not yet implemented")
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}