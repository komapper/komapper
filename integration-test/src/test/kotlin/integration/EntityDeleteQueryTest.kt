package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: Database) {

    @Test
    fun optimisticLockException() {
        val a = Address.alias
        val address = db.runQuery {
            EntityQuery.first(a) {
                a.addressId eq 15
            }
        }
        db.runQuery { EntityQuery.delete(a, address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { EntityQuery.delete(a, address) }
        }
    }

    @Test
    fun testEntity() {
        val a = Address.alias
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { EntityQuery.delete(a, address) }
        assertEquals(emptyList<Address>(), db.runQuery { query })
    }
}
