package org.komapper.dialect.postgresql

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

open class PostgreSqlSchemaStatementBuilder(dialect: PostgreSqlDialect) :
    AbstractSchemaStatementBuilder<PostgreSqlDialect>(dialect) {

    override fun resolveDataTypeName(property: PropertyMetamodel<*, *, *>): String {
        return if (property.isAutoIncrement()) {
            when (property.interiorClass) {
                Int::class -> "serial"
                Long::class -> "bigserial"
                else -> error("Illegal assignment type: ${property.interiorClass.qualifiedName}")
            }
        } else {
            return dialect.getDataTypeName(property.interiorClass)
        }
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return ""
    }
}
