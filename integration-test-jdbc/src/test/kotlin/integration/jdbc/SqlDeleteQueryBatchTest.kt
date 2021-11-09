package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlDeleteQueryBatchTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.runQuery { query }.size)
        db.runQuery { QueryDsl.delete(a).batch(addressList) }
        assertTrue(db.runQuery { query }.isEmpty())
    }

    @Test
    fun optimisticLockException() {
        val a = Address.meta
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.runQuery { query }.size)
        val ex = assertFailsWith<OptimisticLockException> {
            db.runQuery {
                QueryDsl.delete(a).batch(
                    listOf(
                        addressList[0],
                        addressList[1],
                        addressList[2].copy(version = 1)
                    )
                )
            }
        }
        assertEquals("index=2, count=0", ex.message)
    }

    @Test
    fun suppressOptimisticLockException() {
        val a = Address.meta
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.runQuery { query }.size)
        db.runQuery {
            QueryDsl.delete(a)
                .batch(
                    listOf(
                        addressList[0],
                        addressList[1],
                        addressList[2].copy(version = 1)
                    )
                )
                .options {
                    it.copy(suppressOptimisticLockException = true)
                }
        }
    }
}
