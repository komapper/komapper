package org.komapper.template.sql

import java.util.concurrent.ConcurrentHashMap

interface SqlNodeFactory {
    fun get(template: CharSequence): SqlNode
    fun clearCache()
}

class CacheSqlNodeFactory : SqlNodeFactory {
    private val cache = ConcurrentHashMap<String, SqlNode>()

    override fun get(template: CharSequence): SqlNode {
        return cache.computeIfAbsent(template.toString()) { SqlParser(it).parse() }
    }

    override fun clearCache() {
        cache.clear()
    }
}

class NoCacheSqlNodeFactory : SqlNodeFactory {
    override fun get(template: CharSequence): SqlNode {
        return SqlParser(template.toString()).parse()
    }

    override fun clearCache() = Unit
}
