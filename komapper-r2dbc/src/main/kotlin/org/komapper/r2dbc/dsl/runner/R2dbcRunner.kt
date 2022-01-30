package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.runner.Runner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal sealed interface R2dbcRunner<T> : Runner {
    suspend fun run(config: R2dbcDatabaseConfig): T

    data class AndThen<LEFT, RIGHT>(
        val left: R2dbcRunner<LEFT>,
        val right: R2dbcRunner<RIGHT>
    ) : R2dbcRunner<RIGHT> {

        override suspend fun run(config: R2dbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class Map<T, S>(
        val runner: R2dbcRunner<T>,
        val transform: (T) -> S
    ) : R2dbcRunner<S> {

        override suspend fun run(config: R2dbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class Zip<T, S>(
        val left: R2dbcRunner<T>,
        val right: R2dbcRunner<S>
    ) : R2dbcRunner<Pair<T, S>> {

        override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, S> {
            val first = left.run(config)
            val second = right.run(config)
            return first to second
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }
    data class FlatMap<T, S>(
        val runner: R2dbcRunner<T>,
        val transform: (T) -> R2dbcRunner<S>
    ) : R2dbcRunner<S> {

        override suspend fun run(config: R2dbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: R2dbcRunner<T>,
        val transform: (T) -> R2dbcRunner<S>
    ) : R2dbcRunner<Pair<T, S>> {

        override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }
}
