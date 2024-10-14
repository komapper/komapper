package integration.jdbc

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExtendWith(JdbcEnv::class)
class JdbcSessionTest(private val db: JdbcDatabase) {
    @Test
    fun dataSource() {
        assertNotNull(db.config.session.dataSource)
    }
}
