package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

@ThreadSafe
data class RelationInsertValuesContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val values: AssignmentDeclaration<ENTITY, META>,
    override val returning: Returning = Returning.Expressions(emptyList()),
    val options: InsertOptions,
) : TablesProvider, ReturningProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }
}
