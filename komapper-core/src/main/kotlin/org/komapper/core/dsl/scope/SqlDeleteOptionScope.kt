package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.SqlDeleteOption

@Scope
class SqlDeleteOptionScope internal constructor(
    option: SqlDeleteOption
) {

    companion object {
        operator fun SqlDeleteOptionDeclaration.plus(other: SqlDeleteOptionDeclaration): SqlDeleteOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var allowEmptyWhereClause = option.allowEmptyWhereClause

    internal fun asOption(): SqlDeleteOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}
