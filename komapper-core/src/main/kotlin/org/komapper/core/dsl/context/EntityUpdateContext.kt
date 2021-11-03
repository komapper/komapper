package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpdateContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val includedProperties: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    val excludedProperties: List<PropertyMetamodel<ENTITY, *, *>> = emptyList()
) : Context {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    fun getTargetProperties(): List<PropertyMetamodel<ENTITY, *, *>> {
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

    fun asSqlUpdateContext(): SqlUpdateContext<ENTITY, ID, META> {
        return SqlUpdateContext(target)
    }
}
