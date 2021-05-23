package org.komapper.jdbc

import org.komapper.core.ThreadSafe

@ThreadSafe
data class Table(
    val name: String,
    val catalog: String? = null,
    val schema: String? = null,
    val columns: List<Column> = emptyList(),
) {
    fun getCanonicalTableName(enquote: (String) -> String): String {
        return listOfNotNull(catalog, schema, name)
            .filter { it.isNotBlank() }
            .joinToString(".", transform = enquote)
    }
}
