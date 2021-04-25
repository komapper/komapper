package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.runQuery
import org.komapper.jdbc.mysql.MySqlDialect
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val address = Address(16, "STREET 16", 0)
        db.runQuery { EntityQuery.insert(a, address) }
        val address2 = db.runQuery {
            EntityQuery.first(a) {
                a.addressId eq 16
            }
        }
        assertEquals(address, address2)
    }

    @Test
    fun createdAt_localDateTime() {
        val p = Person.alias
        val person1 = Person(1, "ABC")
        val id = db.runQuery { EntityQuery.insert(p, person1) }.personId
        val person2 = db.runQuery { EntityQuery.first(p) { p.personId eq id } }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            EntityQuery.first(p) {
                p.personId to 1
            }
        }
        assertEquals(person2, person3)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun createdAt_offsetDateTime() {
        val h = Human.alias
        val human1 = Human(1, "ABC")
        val id = db.runQuery { EntityQuery.insert(h, human1) }.humanId
        val human2 = db.runQuery { EntityQuery.first(h) { h.humanId eq id } }
        assertNotNull(human2.createdAt)
        assertNotNull(human2.updatedAt)
        assertEquals(human2.createdAt, human2.updatedAt)
        val human3 = db.runQuery {
            EntityQuery.first(h) {
                h.humanId to 1
            }
        }
        assertEquals(human2, human3)
    }

    @Test
    fun createdAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.alias
        val config = object : DatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = Database.create(config)
        val person1 = Person(1, "ABC")
        val id = myDb.runQuery { EntityQuery.insert(p, person1) }
        val person2 = db.runQuery {
            EntityQuery.first(p) {
                p.personId to id
            }
        }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person2.createdAt)
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.runQuery { EntityQuery.insert(a, address) }.let { }
        }
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = IdentityStrategy.alias
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { EntityQuery.insert(m, strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = SequenceStrategy.alias
            val strategy = SequenceStrategy(0, "test")
            val result = db.runQuery { EntityQuery.insert(m, strategy) }
            assertEquals(i, result.id)
        }
    }

    @Test
    fun onDuplicateKeyUpdate_insert() {
        val d = Department.alias
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = EntityQuery.insert(d, department).onDuplicateKeyUpdate()
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { EntityQuery.first(d) { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_insert() {
        val d = Department.alias
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = EntityQuery.insert(d, department).onDuplicateKeyUpdate(d.departmentNo)
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { EntityQuery.first(d) { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdate_update() {
        val d = Department.alias
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = EntityQuery.insert(d, department).onDuplicateKeyUpdate()
        val count = db.runQuery { query }
        if (db.config.dialect is MySqlDialect) {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { EntityQuery.first(d) { d.departmentId eq 1 } }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_update() {
        val d = Department.alias
        val department = Department(6, 10, "PLANNING", "TOKYO", 10)
        val query = EntityQuery.insert(d, department).onDuplicateKeyUpdate(d.departmentNo)
        val count = db.runQuery { query }
        if (db.config.dialect is MySqlDialect) {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { EntityQuery.first(d) { d.departmentNo eq 10 } }
        assertEquals(1, found.departmentId)
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdate_update_set() {
        val d = Department.alias
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = EntityQuery.insert(d, department).onDuplicateKeyUpdate().set { excluded ->
            d.departmentName set "PLANNING2"
            d.location set concat(d.location, concat("_", excluded.location))
        }
        val count = db.runQuery { query }
        if (db.config.dialect is MySqlDialect) {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { EntityQuery.first(d) { d.departmentId eq 1 } }
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKey_update_set() {
        val d = Department.alias
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = EntityQuery.insert(d, department)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName set "PLANNING2"
                d.location set concat(d.location, concat("_", excluded.location))
            }
        val count = db.runQuery { query }
        if (db.config.dialect is MySqlDialect) {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { EntityQuery.first(d) { d.departmentNo eq 10 } }
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyIgnore() {
        val a = Address.alias
        val address = Address(1, "STREET 100", 0)
        val query = EntityQuery.insert(a, address).onDuplicateKeyIgnore()
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKey() {
        val a = Address.alias
        val address = Address(100, "STREET 1", 0)
        val query = EntityQuery.insert(a, address).onDuplicateKeyIgnore(a.street)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() {
        val a = Address.alias
        val address = Address(100, "STREET 1", 0)
        val query = EntityQuery.insert(a, address).onDuplicateKeyIgnore(a.street)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }
}
