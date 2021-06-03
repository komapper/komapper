package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityUpdateBatchOption

data class EntityUpdateBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityUpdateContext<ENTITY, ID, META>,
    val entities: List<ENTITY>,
    val option: EntityUpdateBatchOption
) :
    Query<List<ENTITY>> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
