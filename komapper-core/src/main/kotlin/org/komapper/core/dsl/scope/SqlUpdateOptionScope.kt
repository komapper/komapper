package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.SqlUpdateOption

@Scope
class SqlUpdateOptionScope internal constructor(
    option: SqlUpdateOption
) {

    companion object {
        operator fun SqlUpdateOptionDeclaration.plus(other: SqlUpdateOptionDeclaration): SqlUpdateOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var allowEmptyWhereClause = option.allowEmptyWhereClause

    internal fun asOption(): SqlUpdateOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}
