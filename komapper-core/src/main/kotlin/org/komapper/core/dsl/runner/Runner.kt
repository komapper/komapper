package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface Runner {
    fun dryRun(config: DatabaseConfig): DryRunStatement

    data class AndThen(val left: Runner, val right: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class Map(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class Zip(val left: Runner, val right: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): DryRunStatement {
            return runner.dryRun(config)
        }
    }
}
