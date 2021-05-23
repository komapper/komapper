package org.komapper.jdbc.postgresql

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.PropertyMetamodel

open class PostgreSqlSchemaStatementBuilder(dialect: PostgreSqlJdbcDialect) :
    AbstractSchemaStatementBuilder<PostgreSqlJdbcDialect>(dialect) {

    override fun resolveDataTypeName(property: PropertyMetamodel<*, *, *>): String {
        return if (property.idAssignment is Assignment.AutoIncrement<*, *, *>) {
            when (property.interiorClass) {
                Int::class -> "serial"
                Long::class -> "bigserial"
                else -> error("Illegal assignment type: ${property.interiorClass.qualifiedName}")
            }
        } else {
            val dataType = dialect.getDataType(property.interiorClass)
            dataType.name
        }
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return ""
    }
}
