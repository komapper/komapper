package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import org.komapper.core.dsl.visitor.QueryVisitor

fun <T, S> Query<T>.andThen(other: Query<S>): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.andThenQuery(this@andThen, other)
    }
}

fun <T, S> Query<T>.map(transform: (T) -> S): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.mapQuery(this@map, transform)
    }
}

fun <T, S> Query<T>.zip(other: Query<S>): Query<Pair<T, S>> = object : Query<Pair<T, S>> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.zipQuery(this@zip, other)
    }
}

fun <T, S> Query<T>.flatMap(transform: (T) -> Query<S>): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.flatMapQuery(this@flatMap, transform)
    }
}

fun <T, S> Query<T>.flatZip(transform: (T) -> Query<S>): Query<Pair<T, S>> = object : Query<Pair<T, S>> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.flatZipQuery(this@flatZip, transform)
    }
}

/**
 * Builds a query that returns the first element.
 * When the query is executed, it throws [NoSuchElementException] for empty query result.
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.first(): Query<T> = collect { it.first() }

/**
 * Builds a query that returns the first element or `null` if the query result is empty.
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.firstOrNull(): Query<T?> = collect { it.firstOrNull() }

/**
 * Builds a query that returns the single element.
 * When the query is executed, it throws [NoSuchElementException] for empty query result and [IllegalArgumentException]
 * for query result that contains more than one element.
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.single(): Query<T> = collect { it.single() }

/**
 * Builds a query that returns the single element or `null` if the query result is empty or has more than one element.
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.singleOrNull(): Query<T?> = collect { it.singleOrNull() }
