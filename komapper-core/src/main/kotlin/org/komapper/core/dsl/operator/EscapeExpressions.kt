package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.EscapeExpression

/**
 * Does not escape the given string.
 */
fun <S : CharSequence> text(value: S): EscapeExpression {
    if (value is EscapeExpression) return value
    return EscapeExpression.Text(value.toString())
}

/**
 * Escapes the given string.
 */
fun <S : CharSequence> escape(value: S): EscapeExpression {
    if (value is EscapeExpression) return value
    return EscapeExpression.Escape(value.toString())
}

/**
 * Escapes the given string and appends a wildcard character at the end.
 */
fun CharSequence.asPrefix(): EscapeExpression {
    return escape(this) + text("%")
}

/**
 * Escapes the given string and encloses it with wildcard characters.
 */
fun CharSequence.asInfix(): EscapeExpression {
    return text("%") + escape(this) + text("%")
}

/**
 * Escapes the given string and appends a wildcard character at the beginning.
 */
fun CharSequence.asSuffix(): EscapeExpression {
    return text("%") + escape(this)
}
