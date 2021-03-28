package org.komapper.core.dsl.util

import org.komapper.core.metamodel.Assignment

internal fun Assignment.Sequence<*, *>.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName, this.schemaName, this.name)
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}
