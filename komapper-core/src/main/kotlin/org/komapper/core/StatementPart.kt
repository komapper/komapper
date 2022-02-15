package org.komapper.core

/**
 * The part of the SQL Statement.
 */
@ThreadSafe
sealed class StatementPart : CharSequence {
    data class Text(val text: CharSequence) : StatementPart(), CharSequence by text
    data class Value(val value: org.komapper.core.Value<*>) : StatementPart(), CharSequence by "?"
}
