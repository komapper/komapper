package org.komapper.core.dsl.runner

import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.context.QueryContext
import org.komapper.core.dsl.options.VersionOptions
import org.komapper.core.dsl.options.WhereOptions
import org.komapper.core.dsl.scope.WhereScope

fun checkWhereClause(queryContext: QueryContext, options: WhereOptions) {
    if (!options.allowEmptyWhereClause) {
        val where = queryContext.getCompositeWhere()
        val scope = WhereScope().apply(where)
        if (scope.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
    }
}

fun checkOptimisticLock(
    option: VersionOptions,
    count: Int,
    index: Int?
) {
    if (!option.ignoreVersion && !option.suppressOptimisticLockException) {
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
