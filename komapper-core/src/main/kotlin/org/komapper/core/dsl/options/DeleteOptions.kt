package org.komapper.core.dsl.options

data class DeleteOptions(
    override val allowEmptyWhereClause: Boolean,
    override val batchSize: Int? = null,
    override val escapeSequence: String?,
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean,
) : BatchOptions, VersionOptions, WhereOptions {

    companion object {
        val default = DeleteOptions(
            allowEmptyWhereClause = false,
            batchSize = null,
            escapeSequence = null,
            ignoreVersion = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }
}
