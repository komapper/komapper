package org.komapper.core.expr

import java.util.concurrent.ConcurrentHashMap

interface ExprNodeFactory {
    fun get(expression: String): ExprNode
    fun clear()
}

open class CacheExprNodeFactory : ExprNodeFactory {
    private val cache = ConcurrentHashMap<String, ExprNode>()
    override fun get(expression: String): ExprNode = cache.computeIfAbsent(expression) {
        ExprParser(
            it
        ).parse()
    }

    override fun clear() = cache.clear()
}

open class NoCacheExprNodeFactory : ExprNodeFactory {
    override fun get(expression: String): ExprNode = ExprParser(expression).parse()
    override fun clear() = Unit
}
