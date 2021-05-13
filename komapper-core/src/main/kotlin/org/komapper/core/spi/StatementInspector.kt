package org.komapper.core.spi

import org.komapper.core.Statement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface StatementInspector {
    fun inspect(statement: Statement): Statement
}

class DefaultStatementInspector : StatementInspector {
    override fun inspect(statement: Statement) = statement
}
