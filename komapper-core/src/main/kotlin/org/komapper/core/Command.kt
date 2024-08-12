package org.komapper.core

import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder

sealed interface Command

fun interface FetchOne<T> : Command {
    fun TemplateSelectQueryBuilder.execute(): Query<T>
}

fun interface FetchMany<T> : Command {
    fun TemplateSelectQueryBuilder.execute(): ListQuery<T>
}

fun interface ExecChange : Command {
    fun TemplateExecuteQuery.execute(): Query<Long>
}

abstract class One<T> protected constructor(
    private val fetchOne: FetchOne<T>,
) : FetchOne<T> by fetchOne

abstract class Many<T> protected constructor(
    private val fetchMany: FetchMany<T>,
) : FetchMany<T> by fetchMany

abstract class Exec protected constructor(
    private val execChange: ExecChange = ExecChange { this },
) : ExecChange by execChange

// TODO
abstract class ExecReturnOne<T> protected constructor(
    private val fetchOne: FetchOne<T>,
) : FetchOne<T> by fetchOne

// TODO
abstract class ExecReturnMany<T> protected constructor(
    private val fetchMany: FetchMany<T>,
) : FetchMany<T>
