package org.komapper.core.dsl.element

import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class Relationship<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val metamodel: META,
    val on: OnDeclaration,
)
