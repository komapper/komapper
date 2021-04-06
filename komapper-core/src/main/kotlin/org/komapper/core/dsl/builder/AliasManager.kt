package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.Context
import org.komapper.core.metamodel.Table

internal class AliasManager(context: Context<*>, private val parent: AliasManager? = null) {
    private val aliasMap: Map<Table, String>
    private val index: Int

    init {
        val map: MutableMap<Table, String> = mutableMapOf()
        var i = parent?.index ?: 0
        for (table in context.getTables()) {
            val alias = "t" + i + "_"
            i++
            map[table] = alias
        }
        this.aliasMap = map.toMap()
        this.index = i
    }

    fun getAlias(table: Table): String? {
        if (parent != null) {
            val alias = parent.getAlias(table)
            if (alias != null) {
                return alias
            }
        }
        return aliasMap[table]
    }
}
