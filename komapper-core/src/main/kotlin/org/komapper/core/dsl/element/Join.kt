package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

@ThreadSafe
sealed interface Join<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    val target: META
    val on: OnDeclaration
    val where: WhereDeclaration get() = target.where
}

data class InnerJoin<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val target: META,
    override val on: OnDeclaration,
) : Join<ENTITY, ID, META>

data class LeftJoin<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val target: META,
    override val on: OnDeclaration,
) : Join<ENTITY, ID, META>

data class FullJoin<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val target: META,
    override val on: OnDeclaration,
) : Join<ENTITY, ID, META>
