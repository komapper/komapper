package org.komapper.core.dsl.query

import org.komapper.core.config.Dialect
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import java.sql.ResultSet

internal class EntityMapper(val dialect: Dialect, val resultSet: ResultSet) {
    private var index = 0

    fun <E> execute(e: EntityMetamodel<E>): E? {
        val valueMap = mutableMapOf<PropertyMetamodel<*, *>, Any?>()
        for (p in e.properties()) {
            val value = dialect.getValue(resultSet, ++index, p.klass)
            valueMap[p] = value
        }
        if (valueMap.values.all { it == null }) {
            return null
        }
        return e.instantiate(valueMap)
    }
}
