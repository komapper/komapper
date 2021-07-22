package org.komapper.sqlcommenter

import com.google.cloud.sqlcommenter.schibernate.SCHibernate
import org.komapper.core.Statement
import org.komapper.core.StatementPart
import org.komapper.core.spi.StatementInspector

class SqlCommenterStatementInspector : StatementInspector {

    override fun inspect(statement: Statement): Statement {
        val scHibernate = SCHibernate()
        val comment = scHibernate.inspect(" ").trim()
        return statement.copy(parts = statement.parts + StatementPart.Text(comment))
    }
}
