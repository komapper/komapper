package org.komapper.core

@ThreadSafe
fun interface StatementInspector {
    fun inspect(statement: Statement): Statement
}

object DefaultStatementInspector : StatementInspector {
    override fun inspect(statement: Statement) = statement
}
