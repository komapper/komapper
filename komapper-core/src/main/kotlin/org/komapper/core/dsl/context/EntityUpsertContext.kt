package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val excluded: META = target.newMetamodel(
        table = "excluded",
        catalog = "",
        schema = "",
        alwaysQuote = false
    ),
    val keys: List<PropertyExpression<*>> = emptyList(),
    val duplicateKeyType: DuplicateKeyType,
    val assignmentMap: Map<PropertyExpression<*>, Operand> = createAssignmentMap(target, excluded)
) : Context {

    companion object {
        private fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> createAssignmentMap(
            m1: META,
            m2: META
        ): Map<PropertyExpression<*>, Operand> {
            fun getTargetProperties(meta: META): List<PropertyMetamodel<ENTITY, *>> {
                return meta.properties().filter { it != meta.createdAtProperty() } - meta.idProperties()
            }
            return getTargetProperties(m1).zip(getTargetProperties(m2)).associate { (p1, p2) ->
                p1 to Operand.Property(p2)
            }
        }
    }

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
