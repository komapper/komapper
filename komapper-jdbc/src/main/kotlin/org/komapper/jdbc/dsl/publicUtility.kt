package org.komapper.jdbc.dsl

import org.komapper.jdbc.Database
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.dsl.query.Query
import org.komapper.jdbc.dsl.query.QueryScope

/**
 * Run a query.
 * @param block the Query provider
 */
fun <T> Database.runQuery(block: QueryScope.() -> Query<T>): T {
    return block(QueryScope).run(this.config)
}

fun <T, R> Query<T>.flatMap(transform: (T) -> Query<R>): Query<R> {
    return object : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val result = this@flatMap.run(config)
            return transform(result).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@flatMap.dryRun(config)
        }
    }
}

fun <T, R> Query<T>.flatZip(transform: (T) -> Query<R>): Query<Pair<T, R>> {
    return object : Query<Pair<T, R>> {
        override fun run(config: DatabaseConfig): Pair<T, R> {
            val result = this@flatZip.run(config)
            return result to transform(result).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@flatZip.dryRun(config)
        }
    }
}

infix operator fun <T, S> Query<T>.plus(other: Query<S>): Query<S> {
    return object : Query<S> {
        override fun run(config: DatabaseConfig): S {
            this@plus.run(config)
            return other.run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@plus.dryRun(config) + other.dryRun(config)
        }
    }
}
