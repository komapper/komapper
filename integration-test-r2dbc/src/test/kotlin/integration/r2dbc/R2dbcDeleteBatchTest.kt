package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcDeleteBatchTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.runQuery { query }.size)
        db.runQuery { QueryDsl.delete(a).batch(addressList) }
        val list = db.runQuery { query }
        println(list)
        assertTrue(list.isEmpty())
    }

    @Run(unless = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.delete(a).batch(addressList) }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun optimisticLockException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
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
                        addressList[2].copy(version = 1),
                    ),
                )
            }
        }
        assertEquals("index=2, count=0", ex.message)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun suppressOptimisticLockException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
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
                        addressList[2].copy(version = 1),
                    ),
                )
                .options {
                    it.copy(suppressOptimisticLockException = true)
                }
        }
    }
}
