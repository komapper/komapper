package org.komapper.jdbc

interface JdbcTransactionDefinition {

    operator fun <E : Element> get(key: Key<E>): E?

    infix operator fun plus(other: JdbcTransactionDefinition): JdbcTransactionDefinition {
        return CombinedJdbcTransactionDefinition(this, other)
    }

    interface Key<E : Element>

    interface Element : JdbcTransactionDefinition {
        val key: Key<*>

        override operator fun <E : Element> get(key: Key<E>): E? =
            @Suppress("UNCHECKED_CAST")
            if (this.key == key) this as E else null
    }
}

internal data class CombinedJdbcTransactionDefinition(
    val left: JdbcTransactionDefinition,
    val right: JdbcTransactionDefinition
) : JdbcTransactionDefinition {

    override fun <E : JdbcTransactionDefinition.Element> get(key: JdbcTransactionDefinition.Key<E>): E? {
        val element = right[key]
        if (element != null) {
            return element
        }
        return left[key]
    }
}
