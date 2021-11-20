package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryOptions : ExecutionOptionsProvider {
    val queryTimeoutSeconds: Int?
    val suppressLogging: Boolean

    override fun getExecutionOptions(): ExecutionOptions {
        return ExecutionOptions(
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging
        )
    }
}
