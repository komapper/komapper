package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.runner.Runner
import org.komapper.r2dbc.R2dbcDatabaseConfig

sealed interface R2dbcRunner<T> : Runner {
    suspend fun run(config: R2dbcDatabaseConfig): T

    data class AndThen<LEFT, RIGHT>(
        private val left: R2dbcRunner<LEFT>,
        private val right: R2dbcRunner<RIGHT>,
    ) : R2dbcRunner<RIGHT> {
        private val runner: Runner.AndThen = Runner.AndThen(left, right)

        override fun check(config: DatabaseConfig) {
            left.check(config)
            right.check(config)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class Map<T, S>(
        private val runner: R2dbcRunner<T>,
        private val transform: (T) -> S,
    ) : R2dbcRunner<S> {
        private val coreRunner: Runner.Map = Runner.Map(runner)

        override fun check(config: DatabaseConfig) {
            coreRunner.check(config)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return coreRunner.dryRun(config)
        }
    }

    data class Zip<T, S>(
        private val left: R2dbcRunner<T>,
        private val right: R2dbcRunner<S>,
    ) : R2dbcRunner<Pair<T, S>> {
        private val runner: Runner.Zip = Runner.Zip(left, right)

        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, S> {
            val first = left.run(config)
            val second = right.run(config)
            return first to second
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }
    data class FlatMap<T, S>(
        val runner: R2dbcRunner<T>,
        val transform: (T) -> R2dbcRunner<S>,
    ) : R2dbcRunner<S> {
        private val coreRunner: Runner.FlatMap = Runner.FlatMap(runner)

        override fun check(config: DatabaseConfig) {
            coreRunner.check(config)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return coreRunner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        val runner: R2dbcRunner<T>,
        val transform: (T) -> R2dbcRunner<S>,
    ) : R2dbcRunner<Pair<T, S>> {
        private val coreRunner: Runner.FlatZip = Runner.FlatZip(runner)

        override fun check(config: DatabaseConfig) {
            coreRunner.check(config)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return coreRunner.dryRun(config)
        }
    }
}
