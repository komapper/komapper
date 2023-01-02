package org.komapper.core.dsl.options

data class SelectOptions(
    override val allowMissingWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
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
