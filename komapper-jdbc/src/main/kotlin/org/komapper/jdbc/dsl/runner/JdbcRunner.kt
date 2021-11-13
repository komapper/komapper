package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.runner.Runner
import org.komapper.jdbc.JdbcDatabaseConfig

internal sealed interface JdbcRunner<T> : Runner {
    fun run(config: JdbcDatabaseConfig): T

    data class AndThen<LEFT, RIGHT>(
        val left: JdbcRunner<LEFT>,
        val right: JdbcRunner<RIGHT>
    ) : JdbcRunner<RIGHT> {

        override fun run(config: JdbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class Map<T, S>(
        val runner: JdbcRunner<T>,
        val transform: (T) -> S
    ) : JdbcRunner<S> {

        override fun run(config: JdbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class Zip<T, S>(
        val left: JdbcRunner<T>,
        val right: JdbcRunner<S>
    ) : JdbcRunner<Pair<T, S>> {

        override fun run(config: JdbcDatabaseConfig): Pair<T, S> {
            val first = left.run(config)
            val second = right.run(config)
            return first to second
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap<T, S>(
        val runner: JdbcRunner<T>,
        val transform: (T) -> JdbcRunner<S>
    ) : JdbcRunner<S> {

        override fun run(config: JdbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: JdbcRunner<T>,
        val transform: (T) -> JdbcRunner<S>
    ) : JdbcRunner<Pair<T, S>> {

        override fun run(config: JdbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }
}
