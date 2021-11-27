package org.komapper.core.dsl.options

data class UpdateOptions(
    override val allowEmptyWhereClause: Boolean,
    override val batchSize: Int?,
    override val escapeSequence: String?,
    override val disableOptimisticLock: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : BatchOptions, VersionOptions, WhereOptions {

    companion object {
        val default = UpdateOptions(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            batchSize = null,
            disableOptimisticLock = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }
}
