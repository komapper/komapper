package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions

interface FetchOptions : QueryOptions {
    /**
     * The fetch size used in SELECT queries.
     */
    val fetchSize: Int?

    /**
     * The max row size used in SELECT queries.
     */
    val maxRows: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            fetchSize = fetchSize,
            maxRows = maxRows,
        )
    }
}
