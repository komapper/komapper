package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.first(): Query<T> = collect { it.first() }

/**
 * Builds a query that returns the first element or `null`.
 *
 * @param T the element type of [List]
 * @return the query
 */
fun <T> ListQuery<T>.firstOrNull(): Query<T?> = collect { it.firstOrNull() }
