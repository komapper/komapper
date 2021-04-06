package org.komapper.core.dsl.element

import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.EntityMetamodel

internal sealed class Projection {
    data class Columns(val values: List<Column<*>>) : Projection()
    data class Tables(val values: Set<EntityMetamodel<*>>) : Projection()
}
