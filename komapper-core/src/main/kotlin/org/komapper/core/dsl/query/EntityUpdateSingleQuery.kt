package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption

data class EntityUpdateSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityUpdateContext<ENTITY, ID, META>,
    val option: EntityUpdateOption,
    val entity: ENTITY
) : Query<ENTITY> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
