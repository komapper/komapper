package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: JdbcDatabase) {

    @Test
    fun optimisticLockException() {
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
    fun testEntity() {
        val a = Address.meta
        val query = SqlDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { SqlDsl.delete(a).single(address) }
        assertEquals(emptyList<Address>(), db.runQuery { query })
    }
}
