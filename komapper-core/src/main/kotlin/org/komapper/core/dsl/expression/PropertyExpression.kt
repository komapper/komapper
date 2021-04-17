package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

interface PropertyExpression<T : Any> {
    val owner: EntityExpression<*>
    val klass: KClass<T>
    val name: String
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
