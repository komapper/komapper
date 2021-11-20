package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions

interface BatchOptions : QueryOptions {
    val batchSize: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            batchSize = batchSize
        )
    }
}
