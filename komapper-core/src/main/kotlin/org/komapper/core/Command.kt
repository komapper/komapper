package org.komapper.core

import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder

/**
 * Represents a command in the Komapper framework.
 * A command encapsulates an SQL template, SQL parameters, and SQL execution as a single unit.
 */
sealed interface Command

/**
 * A command to fetch a single result.
 *
 * @param T the type of the result
 */
fun interface FetchOne<T> : Command {
    /**
     * Executes the query to fetch a single result.
     *
     * @return the query to fetch the result
     */
    fun TemplateSelectQueryBuilder.execute(): Query<T>
}

/**
 * A command to fetch multiple results.
 *
 * @param T the type of the results
 */
fun interface FetchMany<T> : Command {
    /**
     * Executes the query to fetch multiple results.
     *
     * @return the query to fetch the results
     */
    fun TemplateSelectQueryBuilder.execute(): ListQuery<T>
}

/**
 * A command to execute a change operation.
 */
fun interface ExecChange : Command {
    /**
     * Executes the change operation.
     *
     * @return the query to execute the change
     */
    fun TemplateExecuteQuery.execute(): Query<Long>
}

/**
 * An abstract class to fetch a single result.
 *
 * @param T the type of the result
 * @property fetchOne the fetch one command
 */
abstract class One<T> protected constructor(
    private val fetchOne: FetchOne<T>,
) : FetchOne<T> by fetchOne

/**
 * An abstract class to fetch multiple results.
 *
 * @param T the type of the results
 * @property fetchMany the fetch many command
 */
abstract class Many<T> protected constructor(
    private val fetchMany: FetchMany<T>,
) : FetchMany<T> by fetchMany

/**
 * An abstract class to execute a change operation.
 *
 * @property execChange the execute change command
 */
abstract class Exec protected constructor(
    private val execChange: ExecChange = ExecChange { this },
) : ExecChange by execChange

/**
 * An abstract class to execute a change operation and return a single result.
 *
 * @param T the type of the result
 * @property fetchOne the fetch one command
 */
abstract class ExecReturnOne<T> protected constructor(
    private val fetchOne: FetchOne<T>,
) : FetchOne<T> by fetchOne

/**
 * An abstract class to execute a change operation and return multiple results.
 *
 * @param T the type of the results
 * @property fetchMany the fetch many command
 */
abstract class ExecReturnMany<T> protected constructor(
    private val fetchMany: FetchMany<T>,
) : FetchMany<T> by fetchMany
