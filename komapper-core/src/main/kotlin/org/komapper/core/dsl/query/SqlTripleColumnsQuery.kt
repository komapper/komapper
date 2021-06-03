package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption

class SqlTripleColumnsQuery<A : Any, B : Any, C : Any>(
    val context: SqlSelectContext<*, *, *>,
    val option: SqlSelectOption,
    val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
) : Subquery<Triple<A?, B?, C?>> {

    override val subqueryContext: SubqueryContext<Triple<A?, B?, C?>>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<Triple<A?, B?, C?>> {
        return Collect(context, option, expressions) { it.first() }
    }

    override fun firstOrNull(): Query<Triple<A?, B?, C?>?> {
        return Collect(context, option, expressions) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<Triple<A?, B?, C?>>) -> R): Query<R> {
        return Collect(context, option, expressions, transform)
    }
    
    override fun except(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: Subquery<Triple<A?, B?, C?>>
    ): SetOperationQuery<Triple<A?, B?, C?>> {
        val setOperatorContext = SqlSetOperationContext(kind, this.subqueryContext, other.subqueryContext)
        return SqlTripleColumnsSetOperationQuery(setOperatorContext, expressions = expressions)
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    class Collect<A : Any, B : Any, C : Any, R>(
        val context: SqlSelectContext<*, *, *>,
        val option: SqlSelectOption,
        val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        val transform: (Sequence<Triple<A?, B?, C?>>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}