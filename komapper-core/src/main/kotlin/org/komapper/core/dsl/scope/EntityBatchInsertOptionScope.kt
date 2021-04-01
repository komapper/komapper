package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.EntityBatchInsertOption
import org.komapper.core.dsl.query.QueryOptionImpl

@Scope
class EntityBatchInsertOptionScope internal constructor(
    option: EntityBatchInsertOption
) {

    companion object {
        operator fun EntityInsertOptionDeclaration.plus(other: EntityInsertOptionDeclaration): EntityInsertOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var batchSize = option.batchSize
    var queryTimeoutSeconds = option.queryTimeoutSeconds

    internal fun asOption(): EntityBatchInsertOption = QueryOptionImpl(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}
