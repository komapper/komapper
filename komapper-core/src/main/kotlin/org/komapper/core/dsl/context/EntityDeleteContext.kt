package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.options.DeleteOptions

@ThreadSafe
data class EntityDeleteContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    override val returning: Returning = Returning.Expressions(emptyList()),
    override val options: DeleteOptions,
) : TablesProvider, WhereProvider, ReturningProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return target.where
    }

    fun asRelationDeleteContext(): RelationDeleteContext<ENTITY, ID, META> {
        return RelationDeleteContext(target, options = options)
    }
}
