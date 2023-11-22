package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarAliasExpression
import org.komapper.core.dsl.expression.ScalarExpression

/**
 * Builds an alias expression.
 */
infix fun <T : Any, S : Any> ColumnExpression<T, S>.alias(alias: String): ColumnExpression<T, S> {
    return AliasExpression(this, alias, true)
}

/**
 * Builds an alias expression.
 */
infix fun <T : Any, S : Any> ScalarExpression<T, S>.alias(alias: String): ScalarExpression<T, S> {
    return ScalarAliasExpression(AliasExpression(this, alias, true), this)
}
