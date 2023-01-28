package org.komapper.core.dsl.options

data class InsertOptions(
    override val batchSize: Int?,
    /**
     * Whether to disable assigning sequence-numbered values to primary keys.
     */
    val disableSequenceAssignment: Boolean,
    val returnGeneratedKeys: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val allowMissingWhereClause: Boolean,
    override val escapeSequence: String?,
) : WhereOptions, BatchOptions, QueryOptions {

    companion object {
        val DEFAULT = InsertOptions(
            batchSize = null,
            disableSequenceAssignment = false,
            returnGeneratedKeys = true,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            allowMissingWhereClause = true,
            escapeSequence = null,
        )
    }
}
