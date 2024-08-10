package org.komapper.core

import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder

sealed interface Command

interface One<T> : Command {
    fun TemplateSelectQueryBuilder.select(): Query<T>
}

interface Many<T : Any> : Command {
    fun TemplateSelectQueryBuilder.select(): ListQuery<T>
}

interface Execute : Command {
    fun TemplateExecuteQuery.execute(): Query<Long> = this
}
