package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.context.inlineViewMetamodel
import org.komapper.core.dsl.context.makeAlias
import org.komapper.core.dsl.metamodel.InlineViewPropertyMetamodel

@ThreadSafe
interface SubqueryExpression<T> {
    val context: SubqueryContext

    operator fun <EXTERIOR : Any, INTERIOR : Any> get(expression: ColumnExpression<EXTERIOR, INTERIOR>): ColumnExpression<EXTERIOR, INTERIOR> {
        val metamodel = context.inlineViewMetamodel
        return if (expression is AliasExpression<*, *>) {
            InlineViewPropertyMetamodel(metamodel, expression as ColumnExpression<EXTERIOR, INTERIOR>, expression.alias)
        } else {
            val alias = context.makeAlias(expression)
            InlineViewPropertyMetamodel(metamodel, expression, alias)
        }
    }
}
