package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.jdbc.DatabaseConfig

internal sealed interface JdbcQueryRunner<T> : QueryRunner {
    fun run(config: DatabaseConfig): T
    fun dryRun(config: DatabaseConfig): String

    data class Plus<LEFT, RIGHT>(
        val left: JdbcQueryRunner<LEFT>,
        val right: JdbcQueryRunner<RIGHT>
    ) : JdbcQueryRunner<RIGHT> {

        override fun run(config: DatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return left.dryRun(config) + ";" + right.dryRun(config)
        }
    }

    data class FlatMap<T, S>(
        val runner: JdbcQueryRunner<T>,
        val transform: (T) -> JdbcQueryRunner<S>
    ) : JdbcQueryRunner<S> {

        override fun run(config: DatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: JdbcQueryRunner<T>,
        val transform: (T) -> JdbcQueryRunner<S>
    ) : JdbcQueryRunner<Pair<T, S>> {

        override fun run(config: DatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return runner.dryRun(config)
        }
    }
}
