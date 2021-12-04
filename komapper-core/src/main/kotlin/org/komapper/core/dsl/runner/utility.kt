package org.komapper.core.dsl.runner

import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.builder.getWhereCriteria
import org.komapper.core.dsl.context.WhereProvider
import org.komapper.core.dsl.options.OptimisticLockOptions

fun checkWhereClause(whereProvider: WhereProvider) {
    if (!whereProvider.options.allowEmptyWhereClause) {
        val criteria = whereProvider.getWhereCriteria()
        if (criteria.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
    }
}

fun checkOptimisticLock(
    option: OptimisticLockOptions,
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
