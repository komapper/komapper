package org.komapper.dialect.mysql.jdbc

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel

open class MySqlSchemaStatementBuilder(dialect: MySqlJdbcDialect) : AbstractSchemaStatementBuilder<MySqlJdbcDialect>(dialect) {
    override fun createSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
}
