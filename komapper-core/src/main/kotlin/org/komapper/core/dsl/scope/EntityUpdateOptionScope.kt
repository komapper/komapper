package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityUpdateOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityUpdateOptionScope internal constructor(
    option: EntityUpdateOption
) {

    companion object {
        operator fun EntityUpdateOptionDeclaration.plus(other: EntityUpdateOptionDeclaration): EntityUpdateOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var ignoreVersion = option.ignoreVersion
    var suppressOptimisticLockException = option.suppressOptimisticLockException

    internal fun asOption(): EntityUpdateOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}
