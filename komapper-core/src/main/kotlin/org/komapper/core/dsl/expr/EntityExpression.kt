package org.komapper.core.dsl.expr

interface EntityExpression {
    fun tableName(): String
    fun catalogName(): String
    fun schemaName(): String

    fun getCanonicalTableName(mapper: (String) -> String): String {
        return listOf(catalogName(), schemaName(), tableName())
            .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
    }
}
