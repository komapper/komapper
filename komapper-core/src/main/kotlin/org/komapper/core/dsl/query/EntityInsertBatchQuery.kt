package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertBatchOption

data class EntityInsertBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityInsertContext<ENTITY, ID, META>,
    val entities: List<ENTITY>,
    val option: EntityInsertBatchOption
) : Query<List<ENTITY>> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
