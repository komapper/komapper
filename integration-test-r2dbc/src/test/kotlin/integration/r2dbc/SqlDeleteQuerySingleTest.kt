package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(Env::class)
class SqlDeleteQuerySingleTest(private val db: R2dbcDatabase) {

    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 15
            }.first()
        }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.delete(a).single(address) }
        }
    }

    @Test
    fun testEntity() = inTransaction(db) {
        val a = Address.meta
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertNull(db.runQuery { query }.firstOrNull())
    }
}
