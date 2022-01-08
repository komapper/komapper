package org.komapper.core.dsl.options

data class DeleteOptions(
    override val allowEmptyWhereClause: Boolean,
    override val batchSize: Int? = null,
    override val escapeSequence: String?,
    override val disableOptimisticLock: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean,
) : BatchOptions, OptimisticLockOptions, WhereOptions {

    companion object {
        val DEFAULT = DeleteOptions(
            allowEmptyWhereClause = false,
            batchSize = null,
            escapeSequence = null,
            disableOptimisticLock = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }
}
