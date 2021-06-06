package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.EntityDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            EntityDsl.from(a).first {
                a.addressId eq 15
            }
        }
        db.runQuery { EntityDsl.delete(a).single(address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { EntityDsl.delete(a).single(address) }
        }
    }

    @Test
    fun testEntity() = inTransaction(db) {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { EntityDsl.delete(a).single(address) }
        assertNull(db.runQuery { query }.firstOrNull())
    }
}
