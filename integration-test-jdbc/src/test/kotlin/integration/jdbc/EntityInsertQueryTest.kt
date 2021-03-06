package integration.jdbc

import integration.Address
import integration.Department
import integration.Human
import integration.IdentityStrategy
import integration.Person
import integration.SequenceStrategy
import integration.meta
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityInsertQueryTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        db.runQuery { EntityDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            EntityDsl.from(a).where {
                a.addressId eq 16
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun createdAt_localDateTime() {
        val p = Person.meta
        val person1 = Person(1, "ABC")
        val id = db.runQuery { EntityDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { EntityDsl.from(p).where { p.personId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            EntityDsl.from(p).where {
                p.personId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun createdAt_offsetDateTime() {
        val h = Human.meta
        val human1 = Human(1, "ABC")
        val id = db.runQuery { EntityDsl.insert(h).single(human1) }.humanId
        val human2 = db.runQuery { EntityDsl.from(h).where { h.humanId eq id }.first() }
        assertNotNull(human2.createdAt)
        assertNotNull(human2.updatedAt)
        assertEquals(human2.createdAt, human2.updatedAt)
        val human3 = db.runQuery {
            EntityDsl.from(h).where {
                h.humanId to 1
            }.first()
        }
        assertEquals(human2, human3)
    }

    @Test
    fun createdAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.meta
        val config = object : JdbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = JdbcDatabase.create(config)
        val person1 = Person(1, "ABC")
        val id = myDb.runQuery { EntityDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            EntityDsl.from(p).where {
                p.personId to id
            }.first()
        }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person2.createdAt)
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.meta
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.runQuery { EntityDsl.insert(a).single(address) }.let { }
        }
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = IdentityStrategy.meta
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = SequenceStrategy.meta
            val strategy = SequenceStrategy(0, "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator_disableSequenceAssignment() {
        val m = SequenceStrategy.meta
        val strategy = SequenceStrategy(50, "test")
        val result = db.runQuery {
            EntityDsl.insert(m).options {
                it.copy(disableSequenceAssignment = true)
            }.single(strategy)
        }
        assertEquals(50, result.id)
    }

    @Test
    fun onDuplicateKeyUpdate_insert() {
        val d = Department.meta
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate().single(department)
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_insert() {
        val d = Department.meta
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdate_update() {
        val d = Department.meta
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate().single(department)
        val count = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(2, count)
            else -> assertEquals(1, count)
        }
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_update() {
        val d = Department.meta
        val department = Department(6, 10, "PLANNING", "TOKYO", 10)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        val count = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(2, count)
            else -> assertEquals(1, count)
        }
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    @Run(unless = [Dbms.MARIADB])
    fun onDuplicateKeyUpdate_update_set() {
        val d = Department.meta
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = EntityDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
            d.departmentName set "PLANNING2"
            d.location set concat(d.location, concat("_", excluded.location))
        }.single(department)
        val count = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(2, count)
            else -> assertEquals(1, count)
        }
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    @Run(unless = [Dbms.MARIADB])
    fun onDuplicateKeyUpdateWithKey_update_set() {
        val d = Department.meta
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = EntityDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName set "PLANNING2"
                d.location set concat(d.location, concat("_", excluded.location))
            }.single(department)
        val count = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(2, count)
            else -> assertEquals(1, count)
        }
        val found = db.runQuery { EntityDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyIgnore() {
        val a = Address.meta
        val address = Address(1, "STREET 100", 0)
        val query = EntityDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKey() {
        val a = Address.meta
        val address = Address(100, "STREET 1", 0)
        val query = EntityDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() {
        val a = Address.meta
        val address = Address(100, "STREET 1", 0)
        val query = EntityDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }
}
