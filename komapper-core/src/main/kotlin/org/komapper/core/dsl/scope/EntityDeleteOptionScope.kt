package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityDeleteOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityDeleteOptionScope internal constructor(
    option: EntityDeleteOption
) {

    companion object {
        operator fun EntityDeleteOptionDeclaration.plus(other: EntityDeleteOptionDeclaration): EntityDeleteOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var ignoreVersion = option.ignoreVersion
    var suppressOptimisticLockException = option.suppressOptimisticLockException

    internal fun asOption(): EntityDeleteOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}
