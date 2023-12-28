package org.komapper.core.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression

object ValueExtractor {
    inline fun <EXTERIOR : Any, INTERIOR : Any> getByIndex(
        expression: ColumnExpression<EXTERIOR, INTERIOR>,
        index: Int,
        block: () -> INTERIOR?,
    ): EXTERIOR? {
        return try {
            val value = block()
            if (value == null) null else expression.wrap(value)
        } catch (e: Exception) {
            val message = "Failed to extract a value from column. The column index is $index. (Column indices start from 0.)"
            throw ValueExtractingException(message, e)
        }
    }

    inline fun <EXTERIOR : Any, INTERIOR : Any> getByName(
        expression: ColumnExpression<EXTERIOR, INTERIOR>,
        block: () -> INTERIOR?,
    ): EXTERIOR? {
        return try {
            val value = block()
            if (value == null) null else expression.wrap(value)
        } catch (e: Exception) {
            val message = "Failed to extract a value from column. The column label is ${expression.columnName}."
            throw ValueExtractingException(message, e)
        }
    }
}
