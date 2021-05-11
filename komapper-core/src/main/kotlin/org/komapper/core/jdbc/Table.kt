package org.komapper.core.jdbc

data class Table(
    val name: String,
    val catalog: String? = null,
    val schema: String? = null,
    val columns: List<Column> = emptyList(),
    val primaryKeys: List<String> = emptyList(),
)
