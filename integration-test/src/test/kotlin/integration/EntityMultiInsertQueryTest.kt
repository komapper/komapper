package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntityMultiInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val ids = db.runQuery { EntityQuery.insertMulti(a, addressList) }
        val list = db.runQuery {
            EntityQuery.from(a).where { a.addressId inList ids }
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
        val ids = db.runQuery { EntityQuery.insertMulti(i, strategies) }
        assertEquals(3, ids.size)
        for (id in ids) {
            assertNotNull(id)
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
        val ids = db.runQuery { EntityQuery.insertMulti(p, personList) }
        val list = db.runQuery { EntityQuery.from(p).where { p.personId inList ids } }
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
                EntityQuery.insertMulti(
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
        val query = EntityQuery.insertMulti(d, listOf(department1, department2)).onDuplicateKeyUpdate()
        db.runQuery { query }
        val list = db.runQuery {
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
        val query =
            EntityQuery.insertMulti(d, listOf(department1, department2)).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName set excluded.departmentName
            }
        db.runQuery { query }
        val list = db.runQuery {
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
        val query = EntityQuery.insertMulti(d, listOf(department1, department2)).onDuplicateKeyIgnore()
        val count = db.runQuery { query }
        assertEquals(1, count)
        val list = db.runQuery {
            EntityQuery.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
        println(list)
    }

    @Test
    fun identity_onDuplicateKeyUpdate() {
        val i = IdentityStrategy.alias
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val query = EntityQuery.insertMulti(i, strategies).onDuplicateKeyUpdate()
        val count = db.runQuery { query }
        assertEquals(3, count)
    }
}
