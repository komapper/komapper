package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ColumnExpression

infix fun <T : Any, S : Any> ColumnExpression<T, S>.alias(alias: String): ColumnExpression<T, S> {
    return AliasExpression(this, alias)
}
