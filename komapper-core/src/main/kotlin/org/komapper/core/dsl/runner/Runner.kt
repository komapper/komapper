package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface Runner {
    fun check(config: DatabaseConfig)

    fun dryRun(config: DatabaseConfig): DryRunStatement

    data class AndThen(private val left: Runner, private val right: Runner) : Runner {
        override fun check(config: DatabaseConfig) {
            left.check(config)
            right.check(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class Map(private val runner: Runner) : Runner {
        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class Zip(private val left: Runner, private val right: Runner) : Runner {
        override fun check(config: DatabaseConfig) {
            left.check(config)
            right.check(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap(private val runner: Runner) : Runner {
        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip(private val runner: Runner) : Runner {
        override fun check(config: DatabaseConfig) {
            runner.check(config)
        }

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }
}
