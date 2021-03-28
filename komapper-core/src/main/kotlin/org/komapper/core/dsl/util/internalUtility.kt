package org.komapper.core.dsl.util

import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal fun TableInfo.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName(), this.schemaName(), this.tableName())
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

internal fun Assignment.Sequence<*, *>.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName, this.schemaName, this.name)
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

internal fun ColumnInfo<*>.getName(mapper: (String) -> String): String {
    return mapper(this.columnName)
}
