package org.komapper.dialect.mysql

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel

open class MySqlSchemaStatementBuilder(dialect: MySqlDialect) : AbstractSchemaStatementBuilder<MySqlDialect>(dialect) {
    override fun createSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
}
