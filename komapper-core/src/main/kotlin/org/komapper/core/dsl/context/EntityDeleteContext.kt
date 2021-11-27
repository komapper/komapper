package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.options.DeleteOptions

data class EntityDeleteContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val options: DeleteOptions = DeleteOptions.default,
) : QueryContext {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return target.where
    }

    fun asRelationDeleteContext(): RelationDeleteContext<ENTITY, ID, META> {
        return RelationDeleteContext(target)
    }
}
