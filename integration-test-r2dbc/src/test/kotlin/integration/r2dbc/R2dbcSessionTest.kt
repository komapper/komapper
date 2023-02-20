package integration.r2dbc

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExtendWith(R2dbcEnv::class)
class R2dbcSessionTest(private val db: R2dbcDatabase) {

    @Test
    fun connectionFactory() {
        assertNotNull(db.config.session.connectionFactory)
    }
}
