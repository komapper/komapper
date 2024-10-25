package integration.jdbc

import integration.core.Address
import integration.core.IdColumnOnlyAddress
import integration.core.Man
import integration.core.Person
import integration.core.address
import integration.core.department
import integration.core.idColumnOnlyAddress
import integration.core.man
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcUpdateBatchTest(private val db: JdbcDatabase) {
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
        val before = db.runQuery { query }
        db.runQuery {
            val updateList = before
                .map { it.copy(street = "[" + it.street + "]") }
                .map { it.copy(version = it.version + 1) }
            QueryDsl.update(a).batch(updateList).options {
                it.copy(disableOptimisticLock = true)
            }
        }
        val after = db.runQuery { query }
        for (each in after) {
            assertTrue(each.street.startsWith("["))
            assertTrue(each.street.endsWith("]"))
        }
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
        val before = db.runQuery { query }
        db.runQuery {
            val updateList = before
                .map { it.copy(street = "[" + it.street + "]") }
                .map { it.copy(version = it.version + 1) }
            QueryDsl.update(a).batch(updateList).options {
                it.copy(suppressOptimisticLockException = true)
            }
        }
        val after = db.runQuery { query }
        for (each in after) {
            assertFalse(each.street.startsWith("["))
            assertFalse(each.street.endsWith("]"))
        }
    }

    @Test
    fun updatedAt() {
        val p = Meta.person
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C"),
        )
        for (person in personList) {
            db.runQuery { QueryDsl.insert(p).single(person) }
        }
        db.runQuery { QueryDsl.update(p).batch(personList) }
        val list = db.runQuery { QueryDsl.from(p).where { p.personId inList listOf(1, 2, 3) } }
        assertTrue(list.all { it.updatedAt != null })
    }

    @Test
    fun uniqueConstraintException() {
        val a = Meta.address
        assertFailsWith<UniqueConstraintException> {
            db.runQuery {
                QueryDsl.update(a).batch(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "B", 1),
                    ),
                )
            }.let { }
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Meta.address
        val ex = assertFailsWith<OptimisticLockException> {
            db.runQuery {
                QueryDsl.update(a).batch(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "C", 2),
                    ),
                )
            }.let { }
        }
        assertEquals("Optimistic lock failed. entity=Address(addressId=3, street=C, version=2), count=0, index=2.", ex.message)
    }

    @Test
    fun include() {
        val d = Meta.department
        val selectQuery = QueryDsl.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]",
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

    @Test
    fun exclude() {
        val d = Meta.department
        val selectQuery = QueryDsl.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]",
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

    @Test
    fun idColumnOnlyEntity() {
        val a = Meta.idColumnOnlyAddress
        val query = QueryDsl.from(a)
        val address: List<IdColumnOnlyAddress> = db.runQuery { query }
        val ex = assertFailsWith<IllegalArgumentException> {
            val updateQuery = QueryDsl.update(a).batch(address)
            db.runQuery { updateQuery }.run { }
        }
        println(ex)
    }

    @Test
    fun nonUpdatableColumn_updateBatch() {
        val p = Meta.man
        val findQuery = QueryDsl.from(p).where { p.manId inList listOf(1, 2, 3) }
        val initialList = listOf(
            Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 2, name = "Bob", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 3, name = "Clair", createdBy = "nobody", updatedBy = "nobody"),
        )
        for (person in initialList) {
            db.runQuery { QueryDsl.insert(p).single(person) }
        }
        val beforeUpdate = db.runQuery { findQuery }
        db.runQuery {
            val updateList = beforeUpdate.map { it.copy(createdBy = "somebody", updatedBy = "somebody") }
            QueryDsl.update(p).batch(updateList)
        }
        val afterUpdate = db.runQuery { findQuery }
        for (person in afterUpdate) {
            assertEquals("nobody", person.createdBy)
            assertEquals("somebody", person.updatedBy)
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
            db.runQuery { QueryDsl.update(p).batch(people) }
            Unit
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
            db.runQuery { QueryDsl.update(p).batch(people + listOf(Person(3, "ccc"))) }
            Unit
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
            QueryDsl.update(p).batch(people).options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
    }
}
