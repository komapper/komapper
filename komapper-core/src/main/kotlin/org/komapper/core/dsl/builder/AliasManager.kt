package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.Context
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal class AliasManager(context: Context<*>, private val parent: AliasManager? = null) {
    private val tableAliasMap: MutableMap<TableInfo, String> = mutableMapOf()
    private val columnAliasMap: MutableMap<ColumnInfo<*>, String> = mutableMapOf()
    private val index: Int

    init {
        var i = parent?.index ?: 0
        for (e in context.getReferencingEntityMetamodels()) {
            val alias = "t" + i + "_"
            i++
            tableAliasMap[e] = alias
            for (p in e.properties()) {
                columnAliasMap[p] = alias
            }
        }
        this.index = i
    }

    fun getAlias(tableInfo: TableInfo): String? {
        return getAlias(
            tableInfo,
            { parent, key -> parent.getAlias(key) },
            { key -> tableAliasMap[key] }
        )
    }

    fun getAlias(columnInfo: ColumnInfo<*>): String? {
        return getAlias(
            columnInfo,
            { parent, key -> parent.getAlias(key) },
            { key -> columnAliasMap[key] }
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
