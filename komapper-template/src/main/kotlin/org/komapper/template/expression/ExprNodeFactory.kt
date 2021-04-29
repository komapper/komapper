package org.komapper.template.expression

import java.util.concurrent.ConcurrentHashMap

internal interface ExprNodeFactory {
    fun get(expression: String): ExprNode
    fun clearCache()
}

internal class CacheExprNodeFactory : ExprNodeFactory {
    private val cache = ConcurrentHashMap<String, ExprNode>()
    override fun get(expression: String): ExprNode {
        return cache.computeIfAbsent(expression) {
            ExprParser(it).parse()
        }
    }

    override fun clearCache() {
        cache.clear()
    }
}

internal class NoCacheExprNodeFactory : ExprNodeFactory {
    override fun get(expression: String): ExprNode {
        return ExprParser(expression).parse()
    }

    override fun clearCache() = Unit
}
