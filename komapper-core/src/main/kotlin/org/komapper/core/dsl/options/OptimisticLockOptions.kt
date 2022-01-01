package org.komapper.core.dsl.options
import org.komapper.core.OptimisticLockException

interface OptimisticLockOptions : QueryOptions {
    /**
     * Whether to disable the optimistic lock.
     * If true is returned, the search condition using version number is not included in the WHERE clause.
     */
    val disableOptimisticLock: Boolean

    /**
     * Whether to suppress [OptimisticLockException].
     */
    val suppressOptimisticLockException: Boolean
}
