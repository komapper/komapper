package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface EntityContext<ENTITY> {
    val mainEntities: List<ENTITY>
    operator fun contains(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean
    fun <T : Any, S : Any> associate(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): OneToMany<T, S>
    fun <T : Any, S : Any, ID : Any> associateById(pair: Pair<EntityMetamodel<T, ID, *>, EntityMetamodel<S, *, *>>): OneToMany<ID, S>
}

@ThreadSafe
interface OneToOne<T, S> : Map<T, S?>

@ThreadSafe
interface OneToMany<T, S> : Map<T, Set<S>> {
    fun asOneToOne(): OneToOne<T, S>
}
