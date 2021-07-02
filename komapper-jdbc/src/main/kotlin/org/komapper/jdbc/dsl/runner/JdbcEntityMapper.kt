package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.jdbc.JdbcDialect
import java.sql.ResultSet

internal class JdbcEntityMapper(val dialect: JdbcDialect, resultSet: ResultSet) {
    private val propertyMapper = JdbcPropertyMapper(dialect, resultSet)

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
