package org.komapper.core.dsl.options

interface VersionOptions : QueryOptions {
    val disableOptimisticLock: Boolean
    val suppressOptimisticLockException: Boolean
}
