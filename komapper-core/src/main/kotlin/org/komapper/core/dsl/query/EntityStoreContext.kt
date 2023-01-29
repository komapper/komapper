package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe

/**
 * Represents a container for an [EntityStore] instance.
 */
@ThreadSafe
interface EntityStoreContext {
    val store: EntityStore
}

internal class EntityStoreContextImpl(
    override val store: EntityStore,
) : EntityStoreContext

/**
 * Converts [EntityStore] to [EntityStoreContext].
 */
fun EntityStore.asContext(): EntityStoreContext {
    return EntityStoreContextImpl(this)
}
