package org.komapper.core.query.builder

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.Context

internal class AliasManager(context: Context<*>, private val parent: AliasManager? = null) {
    private val entityAliasMap: MutableMap<EntityMetamodel<*>, String> = mutableMapOf()
    private val propertyAliasMap: MutableMap<PropertyMetamodel<*, *>, String> = mutableMapOf()
    private val index: Int

    init {
        var i = parent?.index ?: 0
        for (e in context.getEntityMetamodels()) {
            val alias = "t" + i + "_"
            i++
            entityAliasMap[e] = alias
            for (p in e.properties()) {
                propertyAliasMap[p] = alias
            }
        }
        this.index = i
    }

    fun getAlias(entityMetamodel: EntityMetamodel<*>): String? {
        return getAlias(
            entityMetamodel,
            { parent, key -> parent.getAlias(key) },
            { key -> entityAliasMap[key] }
        )
    }

    fun getAlias(propertyMetamodel: PropertyMetamodel<*, *>): String? {
        return getAlias(
            propertyMetamodel,
            { parent, key -> parent.getAlias(key) },
            { key -> propertyAliasMap[key] }
        )
    }

    private fun <KEY> getAlias(
        key: KEY,
        getParentAlias: (AliasManager, KEY) -> String?,
        getCurrentAlias: (KEY) -> String?
    ): String? {
        if (parent != null) {
            val alias = getParentAlias(parent, key)
            if (alias != null) {
                return alias
            }
        }
        return getCurrentAlias(key)
    }
}
