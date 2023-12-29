package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.runner.PropertyMappingException
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet

internal class JdbcEntityMapper(strategy: ProjectionType, dataOperator: JdbcDataOperator, resultSet: ResultSet) {
    private val valueExtractor = when (strategy) {
        ProjectionType.INDEX -> JdbcIndexedValueExtractor(dataOperator, resultSet)
        ProjectionType.NAME -> JdbcNamedValueExtractor(dataOperator, resultSet)
    }

    fun <E : Any> execute(metamodel: EntityMetamodel<E, *, *>, forceMapping: Boolean = false): E? {
        val valueMap = mutableMapOf<PropertyMetamodel<*, *, *>, Any?>()
        for (p in metamodel.properties()) {
            val value = try {
                valueExtractor.execute(p)
            } catch (e: Exception) {
                throw PropertyMappingException(metamodel.klass(), p.name, e)
            }
            valueMap[p] = value
        }
        return if (forceMapping || valueMap.values.any { it != null }) {
            metamodel.newEntity(valueMap)
        } else {
            null
        }
    }
}
