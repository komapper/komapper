package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.TableExpression

@ThreadSafe
interface TablesProvider {
    fun getTables(): Set<TableExpression<*>>
}
