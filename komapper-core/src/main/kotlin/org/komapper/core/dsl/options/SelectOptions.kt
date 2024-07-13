package org.komapper.core.dsl.options

data class SelectOptions(
    override val allowMissingWhereClause: Boolean = DEFAULT.allowMissingWhereClause,
    override val escapeSequence: String? = DEFAULT.escapeSequence,
    override val fetchSize: Int? = DEFAULT.fetchSize,
    override val maxRows: Int? = DEFAULT.maxRows,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
) : FetchOptions, WhereOptions {

    companion object {
        val DEFAULT = SelectOptions(
            allowMissingWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
