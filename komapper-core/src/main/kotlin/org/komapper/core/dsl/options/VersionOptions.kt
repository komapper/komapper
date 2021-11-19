package org.komapper.core.dsl.options

interface VersionOptions : QueryOptions {
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}
