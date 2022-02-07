package org.komapper.core.dsl.expression

import org.komapper.core.dsl.metamodel.EntityMetamodel

sealed class LockTarget {
    object Empty : LockTarget()
    data class Metamodels(val metamodels: List<EntityMetamodel<*, *, *>>) : LockTarget()
}
