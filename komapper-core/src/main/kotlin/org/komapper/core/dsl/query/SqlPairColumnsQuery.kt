package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption

class SqlPairColumnsQuery<A : Any, B : Any>(
    val context: SqlSelectContext<*, *, *>,
    val option: SqlSelectOption,
    val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : Subquery<Pair<A?, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A?, B?>>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<Pair<A?, B?>> {
        return Collect(context, option, expressions) { it.first() }
    }

    override fun firstOrNull(): Query<Pair<A?, B?>?> {
        return Collect(context, option, expressions) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<Pair<A?, B?>>) -> R): Query<R> {
        return Collect(context, option, expressions, transform)
    }

    override fun except(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return setOperation(SqlSetOperationKind.EXCEPT,  other)
    }

    override fun intersect(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return setOperation(SqlSetOperationKind.INTERSECT,  other)
    }

    override fun union(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return setOperation(SqlSetOperationKind.UNION,  other)
    }

    override fun unionAll(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: Subquery<Pair<A?, B?>>
    ): SetOperationQuery<Pair<A?, B?>> {
        val setOperatorContext = SqlSetOperationContext(kind, this.subqueryContext, other.subqueryContext)
        return SqlPairColumnsSetOperationQuery(setOperatorContext, expressions = expressions)
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    class Collect<A : Any, B : Any, R>(
        val context: SqlSelectContext<*, *, *>,
        val option: SqlSelectOption,
        val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        val transform: (Sequence<Pair<A?, B?>>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}