package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

interface ColumnExpression<EXTERIOR : Any, INTERIOR : Any> {
    val owner: TableExpression<*>
    val exteriorClass: KClass<EXTERIOR>
    val interiorClass: KClass<INTERIOR>
    val wrap: (INTERIOR) -> EXTERIOR
    val unwrap: (EXTERIOR) -> INTERIOR
    val columnName: String
    val alwaysQuote: Boolean

    fun getCanonicalColumnName(enquote: (String) -> String): String {
        val transform = if (alwaysQuote) {
            enquote
        } else {
            { it }
        }
        return transform(columnName)
    }
}
