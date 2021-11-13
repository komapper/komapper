package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface Runner {
    fun dryRun(config: DatabaseConfig): Statement

    data class AndThen(val left: Runner, val right: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class Map(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class Zip(val left: Runner, val right: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip(val runner: Runner) : Runner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }
}
