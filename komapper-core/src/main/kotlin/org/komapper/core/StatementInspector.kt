package org.komapper.core

@ThreadSafe
interface StatementInspector {
    fun inspect(statement: Statement): Statement
}

class DefaultStatementInspector : StatementInspector {
    override fun inspect(statement: Statement) = statement
}
