package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlSelectOptions

@Scope
class SqlSelectOptionsScope internal constructor(
    internal var options: SqlSelectOptions
) {

    companion object {
        operator fun SqlSelectOptionsDeclaration.plus(other: SqlSelectOptionsDeclaration): SqlSelectOptionsDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var fetchSize: Int?
        get() = options.fetchSize
        set(value) {
            options = OptionsImpl(fetchSize = value)
        }

    var maxRows: Int?
        get() = options.maxRows
        set(value) {
            options = OptionsImpl(maxRows = value)
        }

    var queryTimeoutSeconds: Int?
        get() = options.queryTimeoutSeconds
        set(value) {
            options = OptionsImpl(queryTimeoutSeconds = value)
        }

    var allowEmptyWhereClause: Boolean
        get() = options.allowEmptyWhereClause
        set(value) {
            options = OptionsImpl(allowEmptyWhereClause = value)
        }
}
