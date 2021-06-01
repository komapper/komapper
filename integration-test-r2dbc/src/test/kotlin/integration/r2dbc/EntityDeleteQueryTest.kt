package integration.r2dbc

import kotlinx.coroutines.flow.firstOrNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            R2dbcEntityDsl.from(a).first {
                a.addressId eq 15
            }
        }
        db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        }
    }

    @Test
    fun testEntity() = inTransaction(db) {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        assertNull(db.runQuery { query }.firstOrNull())
    }
}
