package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement
import kotlin.reflect.KClass

open class PostgreSqlSchemaStatementBuilder(dialect: BuilderDialect) :
    AbstractSchemaStatementBuilder(dialect) {
    override fun <INTERIOR : Any> resolveDataTypeName(property: PropertyMetamodel<*, *, INTERIOR>): String {
        return if (property.isAutoIncrement()) {
            when (val classifier = property.interiorType.classifier) {
                is KClass<*> -> when (classifier) {
                    Int::class -> "serial"
                    Long::class -> "bigserial"
                    else -> error("Illegal assignment type: ${classifier.qualifiedName}")
                }

                else -> error("The classifier type must not be null.")
            }
        } else {
            return super.resolveDataTypeName(property)
        }
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return ""
    }
}
