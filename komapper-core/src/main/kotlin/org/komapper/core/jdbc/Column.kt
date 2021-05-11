package org.komapper.core.jdbc

data class Column(
    val name: String,
    val dataType: Int,
    val typeName: String,
    val length: Int = 0,
    val scale: Int = 0,
    val nullable: Boolean = false,
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
)
