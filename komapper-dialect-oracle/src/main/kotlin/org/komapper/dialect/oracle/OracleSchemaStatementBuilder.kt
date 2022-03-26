package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

open class OracleSchemaStatementBuilder(dialect: BuilderDialect) :
    AbstractSchemaStatementBuilder(dialect) {

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return if (property.isAutoIncrement()) " generated always as identity" else ""
    }
}
