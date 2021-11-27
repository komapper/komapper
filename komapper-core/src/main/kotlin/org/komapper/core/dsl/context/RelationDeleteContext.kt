package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.DeleteOptions

data class RelationDeleteContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val where: WhereDeclaration = {},
    override val options: DeleteOptions = DeleteOptions.default,
) : TablesProvider, WhereProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return target.where + where
    }
}
