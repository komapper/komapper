package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.SortItem

internal interface SortedColumnExpression : SortExpression {
    fun asColumn(): SortItem.Column
}
