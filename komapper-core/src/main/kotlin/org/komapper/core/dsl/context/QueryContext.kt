package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration

@ThreadSafe
interface QueryContext {
    fun getTables(): Set<TableExpression<*>>
    fun getCompositeWhere(): WhereDeclaration = {}
}
