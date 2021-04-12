package org.komapper.jdbc.h2

import org.komapper.core.Scope

@Scope
class EntityMergeOptionScope internal constructor(
    option: EntityMergeOption
) {

    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var ignoreVersion = option.ignoreVersion
    var suppressOptimisticLockException = option.suppressOptimisticLockException

    internal fun asOption(): EntityMergeOption = EntityMergeOption(
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}
