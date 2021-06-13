package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryRunner {
    fun dryRun(config: DatabaseConfig): Statement

    data class Plus(val left: QueryRunner, val right: QueryRunner) : QueryRunner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return left.dryRun(config) + right.dryRun(config)
        }
    }

    data class FlatMap(val runner: QueryRunner) : QueryRunner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }

    data class FlatZip(val runner: QueryRunner) : QueryRunner {

        override fun dryRun(config: DatabaseConfig): Statement {
            return runner.dryRun(config)
        }
    }
}
