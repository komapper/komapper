package org.komapper.core.dsl.options

data class UpdateOptions(
    override val allowMissingWhereClause: Boolean = DEFAULT.allowMissingWhereClause,
    override val batchSize: Int? = DEFAULT.batchSize,
    override val escapeSequence: String? = DEFAULT.escapeSequence,
    override val disableOptimisticLock: Boolean = DEFAULT.disableOptimisticLock,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
    override val suppressOptimisticLockException: Boolean = DEFAULT.suppressOptimisticLockException,
    override val suppressEntityNotFoundException: Boolean = DEFAULT.suppressEntityNotFoundException,
) : BatchOptions, OptimisticLockOptions, MutationOptions, WhereOptions {
    companion object {
        val DEFAULT = UpdateOptions(
            allowMissingWhereClause = false,
            escapeSequence = null,
            batchSize = null,
            disableOptimisticLock = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false,
            suppressEntityNotFoundException = false,
        )
    }
}
