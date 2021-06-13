package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryRunner {
    fun dryRun(config: DatabaseConfig): Statement
}
