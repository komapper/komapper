package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntityBatchInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val results = db.execute { EntityQuery.insertBatch(a, addressList) }
        val list = db.execute {
            EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        }
        assertEquals(list, results)
    }

    @Test
    fun identity() {
        val i = IdentityStrategy.alias
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val results = db.execute { EntityQuery.insertBatch(i, strategies) }
        val list = db.execute { EntityQuery.from(i).orderBy(i.id) }
        assertEquals(list, results)
        for (result in results) {
            assertNotNull(result.id)
            assertNotNull(result.id)
        }
    }

    @Test
    fun createdAt_updatedAt() {
        val p = Person.alias
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        val results = db.execute { EntityQuery.insertBatch(p, personList) }
        for (result in results) {
            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        assertThrows<UniqueConstraintException> {
            db.execute {
                EntityQuery.insertBatch(
                    a,
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0)
                    )
                )
            }.let { }
        }
    }

    @Test
    fun onDuplicateKeyUpdate() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityQuery.insertBatch(d, listOf(department1, department2)).onDuplicateKeyUpdate()
        val (counts, keys) = db.execute { query }
        assertEquals(2, counts.size)
// TODO
//        assertEquals(1, counts[0])
//        assertEquals(1, counts[1])
        assertEquals(2, keys.size)
        assertEquals(0, keys[0])
        assertEquals(0, keys[1])
        val list = db.execute {
            EntityQuery.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
        println(list)
    }

    @Test
    fun onDuplicateKeyUpdate_set() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityQuery.insertBatch(d, listOf(department1, department2)).onDuplicateKeyUpdate().set(d.departmentName)
        val (counts, keys) = db.execute { query }
        assertEquals(2, counts.size)
// TODO
//        assertEquals(1, counts[0])
//        assertEquals(1, counts[1])
        assertEquals(2, keys.size)
        assertEquals(0, keys[0])
        assertEquals(0, keys[1])
        val list = db.execute {
            EntityQuery.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
        println(list)
    }

    @Test
    fun onDuplicateKeyIgnore() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityQuery.insertBatch(d, listOf(department1, department2)).onDuplicateKeyIgnore()
        val (counts, keys) = db.execute { query }
        assertEquals(2, counts.size)
        assertEquals(1, counts[0])
        assertEquals(0, counts[1])
        assertEquals(2, keys.size)
        assertEquals(0, keys[0])
        assertEquals(0, keys[1])
        val list = db.execute {
            EntityQuery.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
        println(list)
    }
}
