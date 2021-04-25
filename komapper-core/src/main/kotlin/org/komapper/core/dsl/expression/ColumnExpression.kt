package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

interface ColumnExpression<T : Any> {
    val owner: TableExpression<*>
    val klass: KClass<T>
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
