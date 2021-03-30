package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlDeleteOptions

@Scope
class SqlDeleteOptionsScope internal constructor(
    internal var options: SqlDeleteOptions
) {

    companion object {
        operator fun SqlDeleteOptionsDeclaration.plus(other: SqlDeleteOptionsDeclaration): SqlDeleteOptionsDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
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
