package org.komapper.core.dsl.builder

import org.komapper.core.metamodel.TableInfo

internal fun TableInfo.getName(): String {
    return listOf(this.catalogName(), this.schemaName(), this.tableName())
        .filter { it != "" }.joinToString(".")
}
