package org.komapper.core.dsl.options

interface OptimisticLockOptions : QueryOptions {
    val disableOptimisticLock: Boolean
    val suppressOptimisticLockException: Boolean
}
