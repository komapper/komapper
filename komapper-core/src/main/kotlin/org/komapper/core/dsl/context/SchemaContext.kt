package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaOptions

@ThreadSafe
data class SchemaContext(
    val metamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val options: SchemaOptions = SchemaOptions.DEFAULT,
)
