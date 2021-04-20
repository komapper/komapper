package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

internal data class EntityUpdateContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val target: META,
    val includedProperties: List<PropertyMetamodel<ENTITY, *>> = emptyList(),
    val excludedProperties: List<PropertyMetamodel<ENTITY, *>> = emptyList()
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }

    fun getTargetProperties(): List<PropertyMetamodel<ENTITY, *>> {
        val idProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val createdAtProperty = target.createdAtProperty()
        val properties = includedProperties.ifEmpty { target.properties() } - excludedProperties
        val versionProperties = if (versionProperty != null && versionProperty !in properties) {
            listOf(versionProperty)
        } else {
            emptyList()
        }
        return properties.filter { it != createdAtProperty } - idProperties + versionProperties
    }
}
