package integration.r2dbc

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
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class InsertValuesTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertNull(key)
    }

    @Test
    fun setIfNotNull() = inTransaction(db) {
        val e = Meta.employee
        val (count, key) = db.runQuery {
            QueryDsl.insert(e).values {
                e.employeeId set 99
                e.departmentId set 1
                e.addressId set 1
                e.employeeName set "aaa"
                e.employeeNo set 99
                e.hiredate set LocalDate.now()
                e.salary set BigDecimal("1000")
                e.version set 1
                e.managerId setIfNotNull null
            }
        }
        assertEquals(1, count)
        assertNull(key)

        val employee = db.runQuery { QueryDsl.from(e).where { e.employeeId eq 99 }.first() }
        assertNull(employee.managerId)
    }

    @Test
    fun generatedKeys_autoIncrement() = inTransaction(db) {
        val a = Meta.identityStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun generatedKeys_sequence() = inTransaction(db) {
        val a = Meta.sequenceStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Test
    fun assignTimestamp_auto() = inTransaction(db) {
        val p = Meta.person
        val (count) = db.runQuery {
            QueryDsl.insert(p).values {
                p.personId set 99
                p.name set "test"
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
    fun assignTimestamp_manual() = inTransaction(db) {
        val p = Meta.person
        val timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
        val (count) = db.runQuery {
            QueryDsl.insert(p).values {
                p.personId set 99
                p.name set "test"
                p.createdAt set timestamp
                p.updatedAt set timestamp
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
    fun assignVersion_auto() = inTransaction(db) {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
            }
        }
        assertEquals(1, count)
        assertNull(key)
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 19 }.first() }
        assertEquals(0, address.version)
    }

    @Test
    fun assignVersion_manual() = inTransaction(db) {
        val a = Meta.address
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 10
            }
        }
        assertEquals(1, count)
        assertNull(key)
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 19 }.first() }
        assertEquals(10, address.version)
    }
}
