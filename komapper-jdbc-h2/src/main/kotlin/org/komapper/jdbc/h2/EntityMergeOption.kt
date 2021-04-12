package org.komapper.jdbc.h2

import org.komapper.core.data.JdbcOption
import org.komapper.core.dsl.query.VersionOption

data class EntityMergeOption(
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption(): JdbcOption {
        return JdbcOption(queryTimeoutSeconds)
    }
}
