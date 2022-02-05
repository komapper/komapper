package integration.r2dbc.h2

import integration.core.H2Setting
import io.r2dbc.spi.ConnectionFactories
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialects

class R2dbcH2Setting : H2Setting<R2dbcDatabaseConfig> {
    companion object {
        const val DRIVER: String = "h2"
        val URL: String = System.getProperty("url") ?: error("The url property is not found.")
    }

    override val config: R2dbcDatabaseConfig =
        DefaultR2dbcDatabaseConfig(ConnectionFactories.get(URL), R2dbcDialects.get(DRIVER))
}
