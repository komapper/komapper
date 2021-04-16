package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

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
            db.execute { EntityQuery.insert(a, address) }
        }
        val query = EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        val before = db.execute { query }
        val updateList = before.map { it.copy(street = "[" + it.street + "]") }
        val results = db.execute { EntityQuery.updateBatch(a, updateList) }
        val after = db.execute {
            EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        }
        assertEquals(after, results)
        for (result in results) {
            assertTrue(result.street.startsWith("["))
            assertTrue(result.street.endsWith("]"))
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
            db.execute { EntityQuery.insert(p, person) }
        }
        val results = db.execute { EntityQuery.updateBatch(p, personList) }
        personList.zip(results).forEach {
            assertNotEquals(it.first.updatedAt, it.second.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        assertThrows<UniqueConstraintException> {
            db.execute {
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
            db.execute {
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
}
