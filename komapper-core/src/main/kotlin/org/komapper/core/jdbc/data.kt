package org.komapper.core.jdbc

data class Table(
    val name: TableName,
    val columns: List<Column>,
    val primaryKeys: List<String>,
)

data class TableName(
    val name: String,
    val catalog: String? = null,
    val schema: String? = null
)

data class Column(
    val name: String,
    val dataType: Int = 0,
    val typeName: String,
    val length: Int = 0,
    val scale: Int = 0,
    val isNullable: Boolean = false,
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
)
