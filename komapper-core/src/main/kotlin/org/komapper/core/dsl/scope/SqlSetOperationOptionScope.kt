package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.SqlSetOperationOption

@Scope
class SqlSetOperationOptionScope internal constructor(
    option: SqlSetOperationOption
) {

    companion object {
        operator fun SqlSetOperationOptionDeclaration.plus(other: SqlSetOperationOptionDeclaration): SqlSetOperationOptionDeclaration {
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

    internal fun asOption(): SqlSetOperationOption = QueryOptionImpl(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}
