package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.SqlDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            SqlDsl.from(a).where {
                a.addressId eq 15
            }.first()
        }
        db.runQuery { SqlDsl.delete(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { SqlDsl.delete(a).single(address) }
        }
    }

    @Test
    fun testEntity() = inTransaction(db) {
        val a = Address.meta
        val query = SqlDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { SqlDsl.delete(a).single(address) }
        assertNull(db.runQuery { query }.firstOrNull())
    }
}
