package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

@ThreadSafe
data class RelationInsertSelectContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val select: SubqueryExpression<ENTITY>,
    val options: InsertOptions,
) : TablesProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    fun asRelationInsertValuesContext(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertValuesContext<ENTITY, ID, META> {
        return RelationInsertValuesContext(
            target = target,
            values = declaration,
            options = options
        )
    }
}
