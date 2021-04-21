package org.komapper.core.dsl.element

import org.komapper.core.dsl.metamodel.EntityMetamodel

internal typealias Association = Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>
