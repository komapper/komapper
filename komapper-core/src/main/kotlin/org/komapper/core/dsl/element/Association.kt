package org.komapper.core.dsl.element

import org.komapper.core.dsl.metamodel.EntityMetamodel

typealias Association = Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>
