package integration.jdbc

import integration.address
import integration.employee
import integration.identityStrategy
import integration.person
import integration.sequenceStrategy
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class InsertValuesTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 0
            }
        }
        assertEquals(1, count)
        assertNull(key)
    }

    @Test
    fun setIfNotNull() {
        val e = Meta.employee
        val (count, key) = db.runQuery {
            QueryDsl.insert(e).values {
                e.employeeId eq 99
                e.departmentId eq 1
                e.addressId eq 1
                e.employeeName eq "aaa"
                e.employeeNo eq 99
                e.hiredate eq LocalDate.now()
                e.salary eq BigDecimal("1000")
                e.version eq 1
                e.managerId eqIfNotNull null
            }
        }
        assertEquals(1, count)
        assertNull(key)

        val employee = db.runQuery { QueryDsl.from(e).where { e.employeeId eq 99 }.first() }
        assertNull(employee.managerId)
    }

    @Test
    fun generatedKeys_autoIncrement() {
        val a = Meta.identityStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.id eq 10
                a.value eq "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun generatedKeys_sequence() {
        val a = Meta.sequenceStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.value eq "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Test
    fun assignTimestamp_auto() {
        val p = Meta.person
        val (count) = db.runQuery {
            QueryDsl.insert(p).values {
                p.personId eq 99
                p.name eq "test"
            }
        }
        assertEquals(1, count)
        val person = db.runQuery {
            QueryDsl.from(p).where { p.personId eq 99 }.first()
        }
        assertNotNull(person.createdAt)
        assertNotNull(person.updatedAt)
    }

    @Test
    fun assignTimestamp_manual() {
        val p = Meta.person
        val timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
        val (count) = db.runQuery {
            QueryDsl.insert(p).values {
                p.personId eq 99
                p.name eq "test"
                p.createdAt eq timestamp
                p.updatedAt eq timestamp
            }
        }
        assertEquals(1, count)
        val person2 = db.runQuery {
            QueryDsl.from(p).where { p.personId eq 99 }.first()
        }
        assertEquals(timestamp, person2.createdAt)
        assertEquals(timestamp, person2.updatedAt)
    }

    @Test
    fun assignVersion_auto() {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
            }
        }
        assertEquals(1, count)
        assertNull(key)
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 19 }.first() }
        assertEquals(0, address.version)
    }

    @Test
    fun assignVersion_manual() {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 10
            }
        }
        assertEquals(1, count)
        assertNull(key)
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 19 }.first() }
        assertEquals(10, address.version)
    }

    @Test
    fun dryRun_sequence() {
        val s = Meta.sequenceStrategy
        val query = QueryDsl.insert(s).values {
            s.value eq "test"
        }
        val result = query.dryRun()
        val expected = """insert into sequence_strategy ("value", id) values (?, ?)"""
        assertEquals(expected, result.sql)
    }

    @Test
    fun dryRun_timestamp() {
        val p = Meta.person
        val query = QueryDsl.insert(p).values {
            p.personId eq 99
            p.name eq "test"
        }
        val result = query.dryRun()
        val expected = "insert into person (person_id, name, created_at, updated_at) values (?, ?, ?, ?)"
        assertEquals(expected, result.sql)
    }

    @Test
    fun dryRun_version() {
        val a = Meta.address
        val query = QueryDsl.insert(a).values {
            a.addressId eq 16
            a.street eq "STREET 16"
        }
        val result = query.dryRun()
        val expected = "insert into address (address_id, street, version) values (?, ?, ?)"
        assertEquals(expected, result.sql)
    }
}
