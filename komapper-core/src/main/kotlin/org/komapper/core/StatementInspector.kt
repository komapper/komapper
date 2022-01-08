package org.komapper.core

/**
 * The SQL statement inspector.
 */
@ThreadSafe
fun interface StatementInspector {
    /**
     * Inspects the SQL statement.
     *
     * @param statement the SQL statement
     * @return the new SQL statement
     */
    fun inspect(statement: Statement): Statement
}

/**
 * The default implementation of [StatementInspector].
 */
object DefaultStatementInspector : StatementInspector {
    override fun inspect(statement: Statement) = statement
}
