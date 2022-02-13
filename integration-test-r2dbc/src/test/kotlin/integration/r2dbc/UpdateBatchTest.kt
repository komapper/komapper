package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Person
import integration.core.Run
import integration.core.address
import integration.core.department
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class UpdateBatchTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test() = inTransaction(db) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        val before = db.runQuery { query }
        db.runQuery {
            val updateList = before.map { it.copy(street = "[" + it.street + "]") }
            QueryDsl.update(a).batch(updateList)
        }
        val after = db.runQuery { query }
        for (each in after) {
            assertTrue(each.street.startsWith("["))
            assertTrue(each.street.endsWith("]"))
        }
    }

    @Run(unless = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test_unsupportedOperationException() = inTransaction(db) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { QueryDsl.insert(a).single(address) }
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(16, 17, 18) }
        val before = db.runQuery { query }
        assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                val updateList = before.map { it.copy(street = "[" + it.street + "]") }
                QueryDsl.update(a).batch(updateList)
            }
            Unit
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun updatedAt() = inTransaction(db) {
        val p = Meta.person
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        for (person in personList) {
            db.runQuery { QueryDsl.insert(p).single(person) }
        }
        db.runQuery { QueryDsl.update(p).batch(personList) }
        val list = db.runQuery { QueryDsl.from(p).where { p.personId inList listOf(1, 2, 3) } }
        assertTrue(list.all { it.updatedAt != null })
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun uniqueConstraintException() = inTransaction(db) {
        val a = Meta.address
        assertFailsWith<UniqueConstraintException> {
            db.runQuery {
                QueryDsl.update(a).batch(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "B", 1)
                    )
                )
            }.let { }
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Meta.address
        val ex = assertFailsWith<OptimisticLockException> {
            db.runQuery {
                QueryDsl.update(a).batch(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "C", 2)
                    )
                )
            }.let { }
        }
        assertEquals("index=2, count=0", ex.message)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun include() = inTransaction(db) {
        val d = Meta.department
        val selectQuery = QueryDsl.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]"
            )
        }
        db.runQuery { QueryDsl.update(d).include(d.departmentName).batch(updateList) }
        val after = db.runQuery { selectQuery }
        for ((b, a) in before.zip(after)) {
            assertTrue(b.version < a.version)
            assertTrue(a.departmentName.startsWith("["))
            assertTrue(a.departmentName.endsWith("]"))
            assertFalse(a.location.startsWith("["))
            assertFalse(a.location.endsWith("]"))
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun exclude() = inTransaction(db) {
        val d = Meta.department
        val selectQuery = QueryDsl.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]"
            )
        }
        db.runQuery { QueryDsl.update(d).exclude(d.location, d.version).batch(updateList) }
        val after = db.runQuery { selectQuery }
        for ((b, a) in before.zip(after)) {
            assertTrue(b.version < a.version)
            assertTrue(a.departmentName.startsWith("["))
            assertTrue(a.departmentName.endsWith("]"))
            assertFalse(a.location.startsWith("["))
            assertFalse(a.location.endsWith("]"))
        }
    }
}
