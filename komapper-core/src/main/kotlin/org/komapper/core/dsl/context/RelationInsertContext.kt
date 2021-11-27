package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.ColumnsAndSource
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

@ThreadSafe
data class RelationInsertContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val columnsAndSource: ColumnsAndSource<ENTITY> = ColumnsAndSource.Values {},
    val options: InsertOptions = InsertOptions.default,
) : TablesProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }
}
