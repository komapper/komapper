package org.komapper.core.dsl.runner

import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.builder.getWhereCriteria
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.WhereProvider
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.OptimisticLockOptions
import org.komapper.core.dsl.scope.AssignmentScope

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
