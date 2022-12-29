package org.komapper.core.dsl.runner

/**
 * @param index the index of the column starting at 0
 * @param cause the cause
 */
class ValueExtractingException(index: Int, cause: Exception) :
    RuntimeException("Failed to extract a value from column. The column index is $index.", cause)
