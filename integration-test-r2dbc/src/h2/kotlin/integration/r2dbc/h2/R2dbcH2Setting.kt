package integration.r2dbc.h2

import integration.core.H2Setting
import org.komapper.core.ExecutionOptions
import org.komapper.r2dbc.R2dbcDatabase

class R2dbcH2Setting : H2Setting<R2dbcDatabase> {
    companion object {
        val URL: String = System.getProperty("url") ?: error("The url property is not found.")
    }

    override val database: R2dbcDatabase = R2dbcDatabase(URL, ExecutionOptions(batchSize = 2))
}
