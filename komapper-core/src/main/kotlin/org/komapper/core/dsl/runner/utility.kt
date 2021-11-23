package org.komapper.core.dsl.runner

import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.options.WhereOptions
import org.komapper.core.dsl.scope.WhereScope

fun checkWhereClause(options: WhereOptions, where: List<WhereDeclaration>) {
    if (!options.allowEmptyWhereClause) {
        val scope = WhereScope().apply { where.forEach { it(this) } }
        if (scope.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
    }
}
