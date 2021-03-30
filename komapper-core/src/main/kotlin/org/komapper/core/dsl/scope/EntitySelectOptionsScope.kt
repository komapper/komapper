package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.EntitySelectOptions
import org.komapper.core.config.OptionsImpl

@Scope
class EntitySelectOptionsScope internal constructor(
    internal var options: EntitySelectOptions
) {

    companion object {
        operator fun EntitySelectOptionsDeclaration.plus(other: EntitySelectOptionsDeclaration): EntitySelectOptionsDeclaration {
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

    var allowEmptyWhere: Boolean
        get() = options.allowEmptyWhereClause
        set(value) {
            options = OptionsImpl(allowEmptyWhereClause = value)
        }
}
