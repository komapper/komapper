package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntityInsertBatchQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val ids = db.runQuery { EntityDsl.insert(a).batch(addressList) }.map { it.addressId }
        val list = db.runQuery {
            EntityDsl.from(a).where { a.addressId inList ids }
        }
        assertEquals(addressList, list)
    }

    @Test
    fun identity() {
        val i = IdentityStrategy.alias
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val results1 = db.runQuery { EntityDsl.insert(i).batch(strategies) }
        val results2 = db.runQuery { EntityDsl.from(i).orderBy(i.id) }
        assertEquals(results1, results2)
        assertTrue(results1.all { it.id != null })
    }

    @Test
    fun createdAt_updatedAt() {
        val p = Person.alias
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        val ids = db.runQuery { EntityDsl.insert(p).batch(personList) }.map { it.personId }
        val list = db.runQuery { EntityDsl.from(p).where { p.personId inList ids } }
        for (person in list) {
            assertNotNull(person.createdAt)
            assertNotNull(person.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        assertThrows<UniqueConstraintException> {
            db.runQuery {
                EntityDsl.insert(
                    a
                ).batch(
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
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate().batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        if (db.config.dialect.subprotocol == "mysql") {
            assertEquals(listOf(1, 2), counts)
        } else {
            assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        if (db.config.dialect.subprotocol == "mysql") {
            assertEquals(listOf(1, 2), counts)
        } else {
            assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdate_set() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query =
            EntityDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName set excluded.departmentName
            }.batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        if (db.config.dialect.subprotocol == "mysql") {
            assertEquals(listOf(1, 2), counts)
        } else {
            assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_set() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            EntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).set { excluded ->
                d.departmentName set excluded.departmentName
            }.batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        if (db.config.dialect.subprotocol == "mysql") {
            assertEquals(listOf(1, 2), counts)
        } else {
            assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyIgnore() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityDsl.insert(d).onDuplicateKeyIgnore().batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        assertEquals(listOf(1, 0), counts)
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = EntityDsl.insert(d).onDuplicateKeyIgnore(d.departmentNo).batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        assertEquals(listOf(1, 0), counts)
        val list = db.runQuery {
            EntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun identity_onDuplicateKeyUpdate() {
        val i = IdentityStrategy.alias
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val query = EntityDsl.insert(i).onDuplicateKeyUpdate().batch(strategies)
        val counts = db.runQuery { query }
        assertEquals(listOf(1, 1, 1), counts)
    }
}
