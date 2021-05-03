package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val insertContext: EntityInsertContext<ENTITY, ID, META>,
    val target: META,
    val excluded: META = target.newMeta(
        table = "excluded",
        catalog = "",
        schema = "",
        alwaysQuote = false
    ),
    val keys: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    val duplicateKeyType: DuplicateKeyType,
    val assignmentMap: Map<PropertyMetamodel<ENTITY, *, *>, Operand> = createAssignmentMap(target, excluded)
) : Context {

    companion object {
        private fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> createAssignmentMap(
            m1: META,
            m2: META
        ): Map<PropertyMetamodel<ENTITY, *, *>, Operand> {
            fun getTargetProperties(meta: META): List<PropertyMetamodel<ENTITY, *, *>> {
                return meta.properties().filter { it != meta.createdAtProperty() } - meta.idProperties()
            }
            return getTargetProperties(m1).zip(getTargetProperties(m2)).associate { (p1, p2) ->
                p1 to Operand.Column(p2)
            }
        }
    }

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
