package integration.r2dbc.h2

import integration.core.H2Setting
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase

class R2dbcH2Setting(url: String) : H2Setting<R2dbcDatabase> {
    override val database: R2dbcDatabase = R2dbcDatabase(
        url = url,
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
