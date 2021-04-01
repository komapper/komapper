package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityBatchDeleteOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityBatchDeleteOptionScope internal constructor(
    option: EntityBatchDeleteOption
) {

    companion object {
        operator fun EntityBatchDeleteOptionDeclaration.plus(other: EntityBatchDeleteOptionDeclaration): EntityBatchDeleteOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var batchSize = option.batchSize
    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var ignoreVersion = option.ignoreVersion
    var suppressOptimisticLockException = option.suppressOptimisticLockException

    internal fun asOption(): EntityBatchDeleteOption = QueryOptionImpl(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}
