package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption

data class EntityUpsertBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityUpsertContext<ENTITY, ID, META>,
    val entities: List<ENTITY>,
    val option: InsertOption
) : Query<List<Int>> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
