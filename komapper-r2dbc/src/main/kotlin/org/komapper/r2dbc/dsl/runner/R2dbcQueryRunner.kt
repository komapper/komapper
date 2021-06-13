package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal sealed interface R2dbcQueryRunner<T> : QueryRunner {
    suspend fun run(config: R2dbcDatabaseConfig): T

    data class Plus<LEFT, RIGHT>(
        val left: R2dbcQueryRunner<LEFT>,
        val right: R2dbcQueryRunner<RIGHT>
    ) : R2dbcQueryRunner<RIGHT> {

        override suspend fun run(config: R2dbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap<T, S>(
        val runner: R2dbcQueryRunner<T>,
        val transform: (T) -> R2dbcQueryRunner<S>
    ) : R2dbcQueryRunner<S> {

        override suspend fun run(config: R2dbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: R2dbcQueryRunner<T>,
        val transform: (T) -> R2dbcQueryRunner<S>
    ) : R2dbcQueryRunner<Pair<T, S>> {

        override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }
}
