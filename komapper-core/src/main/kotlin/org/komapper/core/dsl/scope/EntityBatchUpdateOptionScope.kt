package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityBatchUpdateOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityBatchUpdateOptionScope internal constructor(
    option: EntityBatchUpdateOption
) {

    companion object {
        operator fun EntityUpdateOptionDeclaration.plus(other: EntityUpdateOptionDeclaration): EntityUpdateOptionDeclaration {
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

    internal fun asOption(): EntityBatchUpdateOption = QueryOptionImpl(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}
