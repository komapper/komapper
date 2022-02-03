package org.komapper.dialect.sqlserver

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

open class SqlServerSchemaStatementBuilder(dialect: SqlServerDialect) :
    AbstractSchemaStatementBuilder<SqlServerDialect>(dialect) {

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return if (property.isAutoIncrement()) " identity" else ""
    }
}
