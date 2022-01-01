package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryOptions : ExecutionOptionsProvider {
    /**
     * The query timeout in seconds.
     */
    val queryTimeoutSeconds: Int?

    /**
     * Whether to suppress SQL logging.
     */
    val suppressLogging: Boolean

    override fun getExecutionOptions(): ExecutionOptions {
        return ExecutionOptions(
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging
        )
    }
}
