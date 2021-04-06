package org.komapper.core.dsl.element

import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal sealed class Projection {
    data class Properties(val values: List<PropertyExpression<*>>) : Projection()
    data class Entities(val values: List<EntityMetamodel<*>>) : Projection()
}
