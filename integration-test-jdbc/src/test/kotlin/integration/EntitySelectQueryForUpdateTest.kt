package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.desc
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.EntityDsl

@ExtendWith(Env::class)
class EntitySelectQueryForUpdateTest(private val db: Database) {

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
