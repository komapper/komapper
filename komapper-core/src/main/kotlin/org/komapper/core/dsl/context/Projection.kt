package org.komapper.core.dsl.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal sealed class Projection {
    data class Columns(val values: List<ColumnInfo<*>>) : Projection()
    data class Tables(val values: Set<EntityMetamodel<*>>) : Projection()
}
