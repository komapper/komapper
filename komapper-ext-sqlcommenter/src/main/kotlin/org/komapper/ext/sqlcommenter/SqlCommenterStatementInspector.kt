package org.komapper.ext.sqlcommenter

import com.google.cloud.sqlcommenter.schibernate.SCHibernate
import org.komapper.core.Statement
import org.komapper.core.spi.StatementInspector

class SqlCommenterStatementInspector : StatementInspector {

    override fun inspect(statement: Statement): Statement {
        val scHibernate = SCHibernate()
        val sql = scHibernate.inspect(statement.sql)
        val sqlWithArgs = scHibernate.inspect(statement.sqlWithArgs)
        return statement.copy(sql = sql, sqlWithArgs = sqlWithArgs)
    }
}
