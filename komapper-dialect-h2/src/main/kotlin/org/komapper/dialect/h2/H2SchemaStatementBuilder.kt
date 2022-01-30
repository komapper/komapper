package org.komapper.dialect.h2

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder

open class H2SchemaStatementBuilder(dialect: H2Dialect) : AbstractSchemaStatementBuilder<H2Dialect>(dialect) {
    override fun dropAll(): List<Statement> {
        val buf = StatementBuffer()
        buf.append("drop all objects")
        return listOf(buf.toStatement())
    }
}
