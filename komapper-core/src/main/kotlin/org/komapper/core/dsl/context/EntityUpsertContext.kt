package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpsertContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val target: META,
    val excluded: META = target.newMetamodel(
        table = "excluded",
        catalog = "",
        schema = "",
        alwaysQuote = false
    ),
    val duplicateKeyType: DuplicateKeyType,
    val assignmentOperands: List<Pair<PropertyExpression<*>, Operand>> =
        createAssignmentOperands(target, excluded)
) : Context {
    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }
}

private fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> createAssignmentOperands(
    m1: META,
    m2: META
): List<Pair<PropertyExpression<*>, Operand>> {
    fun getTargetProperties(meta: META): List<PropertyMetamodel<ENTITY, *>> {
        return meta.properties().filter { it != meta.createdAtProperty() } - meta.idProperties()
    }
    return getTargetProperties(m1).zip(getTargetProperties(m2)).map { (p1, p2) ->
        p1 to Operand.Property(p2)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
