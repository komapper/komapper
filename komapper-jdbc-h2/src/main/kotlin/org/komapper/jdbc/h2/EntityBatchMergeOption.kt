package org.komapper.jdbc.h2

import org.komapper.core.data.JdbcOption
import org.komapper.core.dsl.option.BatchOption
import org.komapper.core.dsl.option.VersionOption

data class EntityBatchMergeOption(
    override val batchSize: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false,
) : VersionOption, BatchOption {
    override fun asJdbcOption(): JdbcOption {
        return JdbcOption(
            batchSize = batchSize,
            queryTimeoutSeconds = queryTimeoutSeconds
        )
    }
}
