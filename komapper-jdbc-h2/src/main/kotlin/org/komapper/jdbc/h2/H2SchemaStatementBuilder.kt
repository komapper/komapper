package org.komapper.jdbc.h2

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder

open class H2SchemaStatementBuilder(dialect: H2JdbcDialect) : AbstractSchemaStatementBuilder<H2JdbcDialect>(dialect) {
    override fun dropAll(): Statement {
        return buf.append("drop all objects;").toStatement()
    }
}
