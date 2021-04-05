package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.Context
import org.komapper.core.metamodel.TableInfo

internal class AliasManager(context: Context<*>, private val parent: AliasManager? = null) {
    private val aliasMap: Map<TableInfo, String>
    private val index: Int

    init {
        val map: MutableMap<TableInfo, String> = mutableMapOf()
        var i = parent?.index ?: 0
        for (e in context.getAliasableEntityMetamodels()) {
            val alias = "t" + i + "_"
            i++
            map[e] = alias
        }
        this.aliasMap = map.toMap()
        this.index = i
    }

    fun getAlias(tableInfo: TableInfo): String? {
        if (parent != null) {
            val alias = parent.getAlias(tableInfo)
            if (alias != null) {
                return alias
            }
        }
        return aliasMap[tableInfo]
    }
}
