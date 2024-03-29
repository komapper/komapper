package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.TablesProvider
import org.komapper.core.dsl.expression.TableExpression

interface AliasManager {
    val index: Int
    fun getAlias(expression: TableExpression<*>): String?
}

class DefaultAliasManager(tablesProvider: TablesProvider, private val parent: AliasManager? = null) : AliasManager {
    private val aliasMap: Map<TableExpression<*>, String>
    override val index: Int

    init {
        val map: MutableMap<TableExpression<*>, String> = mutableMapOf()
        var i = parent?.index ?: 0
        for (table in tablesProvider.getTables()) {
            val alias = "t${i}_"
            i++
            map[table] = alias
        }
        this.aliasMap = map.toMap()
        this.index = i
    }

    override fun getAlias(expression: TableExpression<*>): String? {
        if (parent != null) {
            val alias = parent.getAlias(expression)
            if (alias != null) {
                return alias
            }
        }
        return aliasMap[expression]
    }
}

object EmptyAliasManager : AliasManager {
    override val index: Int = 0
    override fun getAlias(expression: TableExpression<*>): String = ""
}
