package integration.jdbc

import integration.core.Address
import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(Env::class)
class DeleteSingleTest(private val db: JdbcDatabase) {

    @Test
    fun optimisticLockException() {
        val a = Meta.address
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
    fun testEntity() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertEquals(emptyList<Address>(), db.runQuery { query })
    }
}
