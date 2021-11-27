package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.UpdateOptions

data class RelationUpdateContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val set: AssignmentDeclaration<ENTITY> = {},
    val where: WhereDeclaration = { },
    val options: UpdateOptions = UpdateOptions.default,
) : QueryContext {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return target.where + where
    }
}
