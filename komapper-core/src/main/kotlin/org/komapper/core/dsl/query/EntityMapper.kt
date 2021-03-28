package org.komapper.core.dsl.query

import org.komapper.core.config.Dialect
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import java.sql.ResultSet

internal class EntityMapper(val dialect: Dialect, val resultSet: ResultSet) {
    private var index = 0

    fun <E> execute(e: EntityMetamodel<E>): E {
        val properties = e.properties()
        val valueMap = mutableMapOf<PropertyMetamodel<*, *>, Any?>()
        for (p in properties) {
            val value = dialect.getValue(resultSet, ++index, p.klass)
            valueMap[p] = value
        }
        return checkNotNull(e.instantiate(valueMap))
    }
}
