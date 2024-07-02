package org.komapper.core.dsl.expression

import kotlin.reflect.KType

sealed interface InteriorExpression<out INTERIOR>

/**
 * The column expression.
 *
 * @param EXTERIOR the exterior type of expression evaluation
 * @param INTERIOR the interior type of expression evaluation
 */
sealed interface ColumnExpression<EXTERIOR : Any, INTERIOR : Any> : SortExpression, InteriorExpression<INTERIOR> {
    val owner: TableExpression<*>
    val exteriorType: KType
    val interiorType: KType
    val wrap: (INTERIOR) -> EXTERIOR
    val unwrap: (EXTERIOR) -> INTERIOR
    val columnName: String
    val alwaysQuote: Boolean
    val masking: Boolean

    fun getCanonicalColumnName(enquote: (String) -> String): String {
        val transform = if (alwaysQuote) {
            enquote
        } else {
            { it }
        }
        return transform(columnName)
    }
}
