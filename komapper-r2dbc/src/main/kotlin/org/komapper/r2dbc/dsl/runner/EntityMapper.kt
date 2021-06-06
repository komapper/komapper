package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.r2dbc.R2dbcDialect

internal class EntityMapper(dialect: R2dbcDialect, row: Row) {
    private val propertyMapper = PropertyMapper(dialect, row)

    fun <E : Any> execute(metamodel: EntityMetamodel<E, *, *>, forceMapping: Boolean = false): E? {
        val valueMap = mutableMapOf<PropertyMetamodel<*, *, *>, Any?>()
        for (p in metamodel.properties()) {
            val value = propertyMapper.execute(p)
            valueMap[p] = value
        }
        return if (forceMapping || valueMap.values.any { it != null }) {
            metamodel.newEntity(valueMap)
        } else {
            null
        }
    }
}
