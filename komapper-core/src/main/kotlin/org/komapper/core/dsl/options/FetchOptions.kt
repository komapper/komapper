package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions

interface FetchOptions : QueryOptions {
    val fetchSize: Int?
    val maxRows: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            fetchSize = fetchSize,
            maxRows = maxRows
        )
    }
}
