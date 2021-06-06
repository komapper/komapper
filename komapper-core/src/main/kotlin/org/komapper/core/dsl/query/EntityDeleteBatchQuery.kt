package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteBatchOption

data class EntityDeleteBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
    val entities: List<ENTITY>,
    val option: EntityDeleteBatchOption
) :
    Query<Unit> {
    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
