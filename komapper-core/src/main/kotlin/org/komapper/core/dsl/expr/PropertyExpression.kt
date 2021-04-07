package org.komapper.core.dsl.expr

import kotlin.reflect.KClass

interface PropertyExpression<T : Any> {
    val owner: EntityExpression<*>
    val klass: KClass<T>
    val columnName: String

    fun getCanonicalColumnName(mapper: (String) -> String): String {
        return mapper(columnName)
    }
}
