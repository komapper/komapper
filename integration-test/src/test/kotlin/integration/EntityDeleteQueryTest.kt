package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: Database) {

    @Test
    fun optimisticLockException() {
        val a = Address.alias
        val address = db.runQuery {
            EntityDsl.first(a) {
                a.addressId eq 15
            }
        }
        db.runQuery { EntityDsl.delete(a, address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { EntityDsl.delete(a, address) }
        }
    }

    @Test
    fun testEntity() {
        val a = Address.alias
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { EntityDsl.delete(a, address) }
        assertEquals(emptyList<Address>(), db.runQuery { query })
    }
}
