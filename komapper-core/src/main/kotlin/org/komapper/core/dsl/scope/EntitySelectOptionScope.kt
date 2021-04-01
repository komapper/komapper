package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntitySelectOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntitySelectOptionScope internal constructor(
    option: EntitySelectOption
) {

    companion object {
        operator fun EntitySelectOptionDeclaration.plus(other: EntitySelectOptionDeclaration): EntitySelectOptionDeclaration {
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

    internal fun asOption(): EntitySelectOption = QueryOptionImpl(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}
