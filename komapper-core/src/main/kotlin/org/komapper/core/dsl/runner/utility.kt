package org.komapper.core.dsl.runner

import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.builder.getWhereCriteria
import org.komapper.core.dsl.context.QueryContext
import org.komapper.core.dsl.options.VersionOptions
import org.komapper.core.dsl.options.WhereOptions

fun checkWhereClause(queryContext: QueryContext, options: WhereOptions) {
    if (!options.allowEmptyWhereClause) {
        val criteria = queryContext.getWhereCriteria()
        if (criteria.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
    }
}

fun checkOptimisticLock(
    option: VersionOptions,
    count: Int,
    index: Int?
) {
    if (!option.disableOptimisticLock && !option.suppressOptimisticLockException) {
        if (count != 1) {
            val message = if (index == null) {
                "count=$count"
            } else {
                "index=$index, count=$count"
            }
            throw OptimisticLockException(message)
        }
    }
}
