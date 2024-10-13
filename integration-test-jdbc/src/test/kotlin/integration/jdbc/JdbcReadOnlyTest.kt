package integration.jdbc

import integration.core.Address
import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcReadOnlyTest(private val db: JdbcDatabase) {
    @Test
    fun list() {
        db.withTransaction(
            TransactionAttribute.REQUIRES_NEW,
            TransactionProperty.ReadOnly(true),
        ) {
            val a = Meta.address
            val list: List<Address> = db.runQuery {
                QueryDsl.from(a)
            }
            assertEquals(15, list.size)
        }
    }
}
