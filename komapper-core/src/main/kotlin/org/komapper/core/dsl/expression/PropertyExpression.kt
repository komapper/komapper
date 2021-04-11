package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

interface PropertyExpression<T : Any> {
    val owner: EntityExpression<*>
    val klass: KClass<T>
    val name: String
    val columnName: String

    fun getCanonicalColumnName(mapper: (String) -> String): String {
        return mapper(columnName)
    }
}
