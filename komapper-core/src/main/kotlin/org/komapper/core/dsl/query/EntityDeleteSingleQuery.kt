package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteOption

// TODO we need interface?
data class EntityDeleteSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
    val entity: ENTITY,
    val option: EntityDeleteOption
) : Query<Unit> {
    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
