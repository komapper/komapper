package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class EntitySelectQueryForUpdateTest(private val db: JdbcDatabase) {

    @Test
    fun forUpdate() {
        val a = Address.meta
        val list = db.runQuery {
            EntityDsl.from(a).where { a.addressId greaterEq 1 }
                .orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
