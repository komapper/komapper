package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions

interface BatchOptions : QueryOptions {
    /**
     * The batch size for INSERT, UPDATE, and DELETE batch queries.
     * If the value is null, the value of [ExecutionOptions.batchSize] will be used.
     */
    val batchSize: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            batchSize = batchSize,
        )
    }
}
