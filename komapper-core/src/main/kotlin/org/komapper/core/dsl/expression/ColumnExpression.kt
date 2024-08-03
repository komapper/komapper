package org.komapper.core.dsl.expression

import kotlin.reflect.KType

sealed interface InteriorExpression<out INTERIOR>

/**
 * Represents a column expression in the DSL.
 * This interface provides methods to access various properties of the column.
 *
 * @param EXTERIOR the exterior type of expression evaluation
 * @param INTERIOR the interior type of expression evaluation
 */
sealed interface ColumnExpression<EXTERIOR : Any, INTERIOR : Any> : SortExpression, InteriorExpression<INTERIOR> {
    /**
     * The owner table expression.
     */
    val owner: TableExpression<*>

    /**
     * The exterior type of the column.
     */
    val exteriorType: KType

    /**
     * The interior type of the column.
     */
    val interiorType: KType

    /**
     * A function that wraps an interior type value to an exterior type value.
     */
    val wrap: (INTERIOR) -> EXTERIOR

    /**
     * A function that unwraps an exterior type value to an interior type value.
     */
    val unwrap: (EXTERIOR) -> INTERIOR

    /**
     * The name of the column.
     */
    val columnName: String

    /**
     * Indicates whether the column name should always be quoted.
     */
    val alwaysQuote: Boolean

    /**
     * Indicates whether the column should be masked.
     */
    val masking: Boolean

    /**
     * Returns the canonical name of the column, optionally quoting it.
     *
     * @param enquote a function that quotes a string
     * @return the canonical name of the column
     */
    fun getCanonicalColumnName(enquote: (String) -> String): String {
        val transform = if (alwaysQuote) {
            enquote
        } else {
            { it }
        }
        return transform(columnName)
    }
}
