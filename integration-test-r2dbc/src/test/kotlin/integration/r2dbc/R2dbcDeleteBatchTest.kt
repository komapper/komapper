package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Person
import integration.core.Run
import integration.core.address
import integration.core.person
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
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
        assertEquals("Optimistic lock failed. entity=Address(addressId=18, street=STREET 18, version=1), count=0, index=2.", ex.message)
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

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun throwEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val people = listOf(
            Person(1, "aaa"),
            Person(2, "bbb"),
            Person(3, "ccc")
        )
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.delete(p).batch(people) }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun throwEntityNotFoundException_at_index_2(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        db.runQuery {
            QueryDsl.insert(p).multiple(
                Person(1, "aaa"),
                Person(2, "bbb")
            )
        }
        val people = db.runQuery { QueryDsl.from(p).orderBy(p.personId) }
        assertEquals(2, people.size)
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.delete(p).batch(people + listOf(Person(3, "ccc"))) }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun suppressEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val people = listOf(
            Person(1, "aaa"),
            Person(2, "bbb"),
            Person(3, "ccc")
        )
        db.runQuery {
            QueryDsl.delete(p).batch(people).options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
    }
}
