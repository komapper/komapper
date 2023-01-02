package org.komapper.core.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression

object ValueExtractor {
    inline fun <EXTERIOR : Any, INTERIOR : Any> execute(
        expression: ColumnExpression<EXTERIOR, INTERIOR>,
        index: Int,
        block: () -> INTERIOR?,
    ): EXTERIOR? {
        return try {
            val value = block()
            if (value == null) null else expression.wrap(value)
        } catch (e: Exception) {
            throw ValueExtractingException(index, e)
        }
    }
}
