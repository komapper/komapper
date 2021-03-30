package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlUpdateOptions

@Scope
class SqlUpdateOptionsScope internal constructor(
    internal var options: SqlUpdateOptions
) {

    companion object {
        operator fun SqlUpdateOptionsDeclaration.plus(other: SqlUpdateOptionsDeclaration): SqlUpdateOptionsDeclaration {
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
