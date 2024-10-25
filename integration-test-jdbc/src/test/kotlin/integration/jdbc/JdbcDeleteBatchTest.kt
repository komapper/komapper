package integration.jdbc

import integration.core.Address
import integration.core.Person
import integration.core.address
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcDeleteBatchTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
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
        assertTrue(db.runQuery { query }.isEmpty())
    }

    @Test
    fun optimisticLockException() {
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

    @Test
    fun suppressOptimisticLockException() {
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

    @Test
    fun disableOptimisticLock() {
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
                    it.copy(disableOptimisticLock = true)
                }
        }
    }

    @Test
    fun throwEntityNotFoundException() {
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

    @Test
    fun throwEntityNotFoundException_at_index_2() {
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

    @Test
    fun suppressEntityNotFoundException() {
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
