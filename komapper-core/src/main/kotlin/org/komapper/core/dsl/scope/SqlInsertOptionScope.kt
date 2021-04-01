package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.SqlInsertOption

@Scope
class SqlInsertOptionScope internal constructor(
    option: SqlInsertOption
) {

    companion object {
        operator fun SqlInsertOptionDeclaration.plus(other: SqlInsertOptionDeclaration): SqlInsertOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds

    internal fun asOption(): SqlInsertOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}
