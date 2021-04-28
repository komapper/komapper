package org.komapper.core.spi

import org.komapper.core.Statement

interface StatementInspector {
    fun inspect(statement: Statement): Statement
}

class DefaultStatementInspector : StatementInspector {
    override fun inspect(statement: Statement) = statement
}
