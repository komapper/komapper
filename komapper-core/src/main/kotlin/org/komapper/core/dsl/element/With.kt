package org.komapper.core.dsl.element

import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class With(
    val recursive: Boolean,
    val pairs: List<Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>>,
)
