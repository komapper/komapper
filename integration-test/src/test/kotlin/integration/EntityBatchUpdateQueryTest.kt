package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntityBatchUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.runQuery { EntityQuery.insert(a, address) }
        }
        val query = EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        val before = db.runQuery { query }
        db.runQuery {
            val updateList = before.map { it.copy(street = "[" + it.street + "]") }
            EntityQuery.updateBatch(a, updateList)
        }
        val after = db.runQuery { query }
        for (each in after) {
            assertTrue(each.street.startsWith("["))
            assertTrue(each.street.endsWith("]"))
        }
    }

    @Test
    fun updatedAt() {
        val p = Person.alias
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        for (person in personList) {
            db.runQuery { EntityQuery.insert(p, person) }
        }
        db.runQuery { EntityQuery.updateBatch(p, personList) }
        val list = db.runQuery { EntityQuery.from(p).where { p.personId inList listOf(1, 2, 3) } }
        assertTrue(list.all { it.updatedAt != null })
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        assertThrows<UniqueConstraintException> {
            db.runQuery {
                EntityQuery.updateBatch(
                    a,
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "B", 1)
                    )
                )
            }.let { }
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Address.alias
        val ex = assertThrows<OptimisticLockException> {
            db.runQuery {
                EntityQuery.updateBatch(
                    a,
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

    @Test
    fun include() {
        val d = Department.alias
        val selectQuery = EntityQuery.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]"
            )
        }
        db.runQuery { EntityQuery.updateBatch(d, updateList).include(d.departmentName) }
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
        val d = Department.alias
        val selectQuery = EntityQuery.from(d).where { d.departmentId inList listOf(1, 2) }
        val before = db.runQuery { selectQuery }
        val updateList = before.map {
            it.copy(
                departmentName = "[" + it.departmentName + "]",
                location = "[" + it.location + "]"
            )
        }
        db.runQuery { EntityQuery.updateBatch(d, updateList).exclude(d.location, d.version) }
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
