package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.runner.Runner
import org.komapper.jdbc.JdbcDatabaseConfig

sealed interface JdbcRunner<T> : Runner {
    fun run(config: JdbcDatabaseConfig): T

    data class AndThen<LEFT, RIGHT>(
        private val left: JdbcRunner<LEFT>,
        private val right: JdbcRunner<RIGHT>
    ) : JdbcRunner<RIGHT> {

        private val runner: Runner.AndThen = Runner.AndThen(left, right)

        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override fun run(config: JdbcDatabaseConfig): RIGHT {
            left.run(config)
            return right.run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class Map<T, S>(
        private val runner: JdbcRunner<T>,
        private val transform: (T) -> S
    ) : JdbcRunner<S> {

        private val _runner: Runner.Map = Runner.Map(runner)

        override fun check(config: DatabaseConfig) {
            _runner.check(config)
        }

        override fun run(config: JdbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return _runner.dryRun(config)
        }
    }

    data class Zip<T, S>(
        private val left: JdbcRunner<T>,
        private val right: JdbcRunner<S>
    ) : JdbcRunner<Pair<T, S>> {

        private val runner: Runner.Zip = Runner.Zip(left, right)

        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override fun run(config: JdbcDatabaseConfig): Pair<T, S> {
            val first = left.run(config)
            val second = right.run(config)
            return first to second
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class FlatMap<T, S>(
        private val runner: JdbcRunner<T>,
        private val transform: (T) -> JdbcRunner<S>
    ) : JdbcRunner<S> {

        private val _runner: Runner.FlatMap = Runner.FlatMap(runner)

        override fun check(config: DatabaseConfig) {
            _runner.check(config)
        }

        override fun run(config: JdbcDatabaseConfig): S {
            val value = runner.run(config)
            return transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return _runner.dryRun(config)
        }
    }

    data class FlatZip<T, S>(
        private val runner: JdbcRunner<T>,
        private val transform: (T) -> JdbcRunner<S>
    ) : JdbcRunner<Pair<T, S>> {

        private val _runner: Runner.FlatZip = Runner.FlatZip(runner)

        override fun check(config: DatabaseConfig) {
            _runner.check(config)
        }

        override fun run(config: JdbcDatabaseConfig): Pair<T, S> {
            val value = runner.run(config)
            return value to transform(value).run(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return _runner.dryRun(config)
        }
    }
}
