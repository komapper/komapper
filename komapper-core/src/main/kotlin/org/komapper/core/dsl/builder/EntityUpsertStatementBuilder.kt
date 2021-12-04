package org.komapper.core.dsl.builder

import org.komapper.core.Statement
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

interface EntityUpsertStatementBuilder<ENTITY : Any> {
    fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement
}
