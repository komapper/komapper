package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression

@ThreadSafe
sealed class ColumnsAndSource<ENTITY : Any> {
    data class Values<ENTITY : Any>(val declaration: AssignmentDeclaration<ENTITY>) : ColumnsAndSource<ENTITY>()
    data class Subquery<ENTITY : Any>(val expression: SubqueryExpression<*>) : ColumnsAndSource<ENTITY>()
}
