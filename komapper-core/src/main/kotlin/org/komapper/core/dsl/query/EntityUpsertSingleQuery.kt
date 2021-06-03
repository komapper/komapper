package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption

data class EntityUpsertSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityUpsertContext<ENTITY, ID, META>,
    val option: InsertOption,
    val entity: ENTITY,
) : Query<Int> {
    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
