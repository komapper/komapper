package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.Context
import org.komapper.core.dsl.expr.EntityExpression

internal class AliasManager(context: Context<*>, private val parent: AliasManager? = null) {
    private val aliasMap: Map<EntityExpression, String>
    private val index: Int

    init {
        val map: MutableMap<EntityExpression, String> = mutableMapOf()
        var i = parent?.index ?: 0
        for (expression in context.getEntityExpressions()) {
            val alias = "t${i}_"
            i++
            map[expression] = alias
        }
        this.aliasMap = map.toMap()
        this.index = i
    }

    fun getAlias(expression: EntityExpression): String? {
        if (parent != null) {
            val alias = parent.getAlias(expression)
            if (alias != null) {
                return alias
            }
        }
        return aliasMap[expression]
    }
}
