package org.komapper.core.dsl.expr

import kotlin.reflect.KClass

interface EntityExpression<T : Any> {
    fun klass(): KClass<T>
    fun tableName(): String
    fun catalogName(): String
    fun schemaName(): String

    fun getCanonicalTableName(mapper: (String) -> String): String {
        return listOf(catalogName(), schemaName(), tableName())
            .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
    }
}
