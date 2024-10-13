package org.komapper.core.dsl.options

data class InsertOptions(
    override val batchSize: Int? = DEFAULT.batchSize,
    /**
     * Whether to disable assigning sequence-numbered values to primary keys.
     */
    val disableSequenceAssignment: Boolean = DEFAULT.disableSequenceAssignment,
    val returnGeneratedKeys: Boolean = DEFAULT.returnGeneratedKeys,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
    override val allowMissingWhereClause: Boolean = DEFAULT.allowMissingWhereClause,
    override val escapeSequence: String? = DEFAULT.escapeSequence,
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
