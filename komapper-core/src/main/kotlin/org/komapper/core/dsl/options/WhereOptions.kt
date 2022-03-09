package org.komapper.core.dsl.options
import org.komapper.core.Dialect

interface WhereOptions : QueryOptions {
    /**
     * Whether to allow missing where clause.
     */
    val allowMissingWhereClause: Boolean

    /**
     * The escape sequence to be used in the LIKE predicate.
     * If null is returned, the value of [Dialect.escapeSequence] will be used.
     */
    val escapeSequence: String?
}
