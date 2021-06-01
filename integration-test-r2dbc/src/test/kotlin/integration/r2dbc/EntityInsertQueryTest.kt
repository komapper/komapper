package integration.r2dbc

import integration.r2dbc.setting.Dbms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.concat
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.dsl.R2dbcEntityDsl
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityInsertQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        db.runQuery { R2dbcEntityDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            R2dbcEntityDsl.from(a).first {
                a.addressId eq 16
            }
        }
        assertEquals(address, address2)
    }

    @Test
    fun createdAt_localDateTime() = inTransaction(db) {
        val p = Person.meta
        val person1 = Person(1, "ABC")
        val id = db.runQuery { R2dbcEntityDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { R2dbcEntityDsl.from(p).first { p.personId eq id } }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            R2dbcEntityDsl.from(p).first {
                p.personId to 1
            }
        }
        assertEquals(person2, person3)
    }

    // TODO
    @Disabled
    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun createdAt_offsetDateTime() = inTransaction(db) {
        val h = Human.meta
        val human1 = Human(1, "ABC")
        val id = db.runQuery { R2dbcEntityDsl.insert(h).single(human1) }.humanId
        val human2 = db.runQuery { R2dbcEntityDsl.from(h).first { h.humanId eq id } }
        assertNotNull(human2.createdAt)
        assertNotNull(human2.updatedAt)
        assertEquals(human2.createdAt, human2.updatedAt)
        val human3 = db.runQuery {
            R2dbcEntityDsl.from(h).first {
                h.humanId to 1
            }
        }
        assertEquals(human2, human3)
    }

    @Test
    fun createdAt_customize() = inTransaction(db) {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.meta
        val config = object : R2dbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = R2dbcDatabase.create(config)
        val person1 = Person(1, "ABC")
        val id = myDb.runQuery { R2dbcEntityDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            R2dbcEntityDsl.from(p).first {
                p.personId to id
            }
        }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person2.createdAt)
    }

    @Test
    fun uniqueConstraintException() = inTransaction(db) {
        val a = Address.meta
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.runQuery { R2dbcEntityDsl.insert(a).single(address) }.let { }
        }
    }

    @Test
    fun identityGenerator() = inTransaction(db) {
        for (i in 1..201) {
            val m = IdentityStrategy.meta
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { R2dbcEntityDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() = inTransaction(db) {
        for (i in 1..201) {
            val m = SequenceStrategy.meta
            val strategy = SequenceStrategy(0, "test")
            val result = db.runQuery { R2dbcEntityDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator_disableSequenceAssignment() = inTransaction(db) {
        val m = SequenceStrategy.meta
        val strategy = SequenceStrategy(50, "test")
        val result = db.runQuery {
            R2dbcEntityDsl.insert(m).option {
                it.copy(disableSequenceAssignment = true)
            }.single(strategy)
        }
        assertEquals(50, result.id)
    }

    @Test
    fun onDuplicateKeyUpdate_insert() = inTransaction(db) {
        val d = Department.meta
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate().single(department)
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_insert() = inTransaction(db) {
        val d = Department.meta
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        val count = db.runQuery { query }
        assertEquals(1, count)
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdate_update() = inTransaction(db) {
        val d = Department.meta
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate().single(department)
        val count = db.runQuery { query }
        if (db.config.dialect.driver == "mysql") {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentId eq 1 } }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_update() = inTransaction(db) {
        val d = Department.meta
        val department = Department(6, 10, "PLANNING", "TOKYO", 10)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        val count = db.runQuery { query }
        if (db.config.dialect.driver == "mysql") {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentNo eq 10 } }
        assertEquals(1, found.departmentId)
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdate_update_set() = inTransaction(db) {
        val d = Department.meta
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
            d.departmentName set "PLANNING2"
            d.location set concat(d.location, concat("_", excluded.location))
        }.single(department)
        val count = db.runQuery { query }
        if (db.config.dialect.driver == "mysql") {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentId eq 1 } }
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKey_update_set() = inTransaction(db) {
        val d = Department.meta
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = R2dbcEntityDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName set "PLANNING2"
                d.location set concat(d.location, concat("_", excluded.location))
            }.single(department)
        val count = db.runQuery { query }
        if (db.config.dialect.driver == "mysql") {
            assertEquals(2, count)
        } else {
            assertEquals(1, count)
        }
        val found = db.runQuery { R2dbcEntityDsl.from(d).first { d.departmentNo eq 10 } }
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyIgnore() = inTransaction(db) {
        val a = Address.meta
        val address = Address(1, "STREET 100", 0)
        val query = R2dbcEntityDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKey() = inTransaction(db) {
        val a = Address.meta
        val address = Address(100, "STREET 1", 0)
        val query = R2dbcEntityDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() = inTransaction(db) {
        val a = Address.meta
        val address = Address(100, "STREET 1", 0)
        val query = R2dbcEntityDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }
}
