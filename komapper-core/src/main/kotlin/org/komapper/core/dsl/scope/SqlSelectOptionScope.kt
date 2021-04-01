package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.SqlSelectOption

@Scope
class SqlSelectOptionScope internal constructor(
    option: SqlSelectOption
) {

    companion object {
        operator fun SqlSelectOptionDeclaration.plus(other: SqlSelectOptionDeclaration): SqlSelectOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var fetchSize = option.fetchSize
    var maxRows = option.maxRows
    var queryTimeoutSeconds = option.queryTimeoutSeconds
    var allowEmptyWhereClause = option.allowEmptyWhereClause

    internal fun asOption(): SqlSelectOption = QueryOptionImpl(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}
