package org.komapper.core

import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder

sealed interface Command

interface One<T> : Command {
    fun TemplateSelectQueryBuilder.execute(): Query<T>
}

interface Many<T : Any> : Command {
    fun TemplateSelectQueryBuilder.execute(): ListQuery<T>
}

interface Exec : Command {
    fun TemplateExecuteQuery.execute(): Query<Long> = this
}
