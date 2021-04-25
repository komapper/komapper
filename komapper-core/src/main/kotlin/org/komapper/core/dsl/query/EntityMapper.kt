package org.komapper.core.dsl.query

import org.komapper.core.Dialect
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import java.sql.ResultSet

internal class EntityMapper(val dialect: Dialect, resultSet: ResultSet) {
    private val propertyMapper = PropertyMapper(dialect, resultSet)

    fun <E : Any> execute(metamodel: EntityMetamodel<E, *, *>, forceMapping: Boolean = false): E? {
        val valueMap = mutableMapOf<PropertyMetamodel<*, *>, Any?>()
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
