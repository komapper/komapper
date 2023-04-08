package org.komapper.core.dsl.runner

import org.komapper.core.dsl.metamodel.EntityMetamodel

@Deprecated("never used")
data class EntityKey(
    val entityMetamodel: EntityMetamodel<*, *, *>,
    val id: Any,
)
