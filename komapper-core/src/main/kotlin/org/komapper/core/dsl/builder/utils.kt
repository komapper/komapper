package org.komapper.core.dsl.builder

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal fun TableInfo.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName(), this.schemaName(), this.tableName())
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

internal fun ColumnInfo<*>.getName(mapper: (String) -> String): String {
    return mapper(this.columnName)
}
