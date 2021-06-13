package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal sealed interface JdbcQueryRunner<T> : QueryRunner {
    fun run(config: JdbcDatabaseConfig): T

    data class Plus<LEFT, RIGHT>(
        val left: JdbcQueryRunner<LEFT>,
        val right: JdbcQueryRunner<RIGHT>
    ) : JdbcQueryRunner<RIGHT> {

        override fun run(config: JdbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap<T, S>(
        val runner: JdbcQueryRunner<T>,
        val transform: (T) -> JdbcQueryRunner<S>
    ) : JdbcQueryRunner<S> {

        override fun run(config: JdbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: JdbcQueryRunner<T>,
        val transform: (T) -> JdbcQueryRunner<S>
    ) : JdbcQueryRunner<Pair<T, S>> {

        override fun run(config: JdbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }
}
