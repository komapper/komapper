package org.komapper.jdbc.postgresql

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.PropertyMetamodel

open class PostgreSqlSchemaStatementBuilder(dialect: PostgreSqlDialect) :
    AbstractSchemaStatementBuilder<PostgreSqlDialect>(dialect) {

    override fun resolveDataTypeName(property: PropertyMetamodel<*, *>): String {
        return if (property.idAssignment is Assignment.AutoIncrement<*, *>) {
            when (property.klass) {
                Int::class -> "serial"
                Long::class -> "bigserial"
                else -> error("Illegal assignment type: ${property.klass.qualifiedName}")
            }
        } else {
            val dataType = dialect.getDataType(property.klass)
            dataType.name
        }
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *>): String {
        return ""
    }
}
