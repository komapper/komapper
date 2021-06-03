package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption

class SqlSingleColumnQuery<A : Any>(
    val context: SqlSelectContext<*, *, *>,
    val option: SqlSelectOption,
    val expression: ColumnExpression<A, *>
) : Subquery<A?> {

    override val subqueryContext: SubqueryContext<A?>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<A?> {
        return Collect(context, option, expression) { it.first() }
    }

    override fun firstOrNull(): Query<A?> {
        return Collect(context, option, expression) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<A?>) -> R): Query<R> {
        return Collect(context, option, expression, transform)
    }

    override fun except(other: Subquery<A?>): SetOperationQuery<A?> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: Subquery<A?>): SetOperationQuery<A?> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: Subquery<A?>): SetOperationQuery<A?> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: Subquery<A?>): SetOperationQuery<A?> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SqlSetOperationKind, other: Subquery<A?>): SetOperationQuery<A?> {
        val setOperatorContext = SqlSetOperationContext(kind, this.subqueryContext, other.subqueryContext)
        return SqlSingleColumnSetOperationQuery(setOperatorContext, expression = expression)
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    class Collect<A : Any, R>(
        val context: SqlSelectContext<*, *, *>,
        val option: SqlSelectOption,
        val expression: ColumnExpression<A, *>,
        val transform: (Sequence<A?>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}