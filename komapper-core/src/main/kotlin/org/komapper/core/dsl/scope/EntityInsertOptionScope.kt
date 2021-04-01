package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityInsertOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityInsertOptionScope internal constructor(
    option: EntityInsertOption
) {

    companion object {
        operator fun EntityInsertOptionDeclaration.plus(other: EntityInsertOptionDeclaration): EntityInsertOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds

    internal fun asOption(): EntityInsertOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds,
    )
}
