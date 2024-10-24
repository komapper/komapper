package org.komapper.core.dsl.options

data class DeleteOptions(
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
        val DEFAULT = DeleteOptions(
            allowMissingWhereClause = false,
            batchSize = null,
            escapeSequence = null,
            disableOptimisticLock = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false,
            suppressEntityNotFoundException = false,
        )
    }
}
