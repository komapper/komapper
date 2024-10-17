package integration.r2dbc

import integration.core.Dbms
import integration.core.RobotInfo1
import integration.core.RobotInfo2
import integration.core.Run
import integration.core.address
import integration.core.employee
import integration.core.identityStrategy
import integration.core.person
import integration.core.robot
import integration.core.sequenceStrategy
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertValuesTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
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
    fun setIfNotNull(info: TestInfo) = inTransaction(db, info) {
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
    fun generatedKeys_autoIncrement(info: TestInfo) = inTransaction(db, info) {
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

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun generatedKeys_sequence(info: TestInfo) = inTransaction(db, info) {
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
    fun assignTimestamp_auto(info: TestInfo) = inTransaction(db, info) {
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
    fun assignTimestamp_manual(info: TestInfo) = inTransaction(db, info) {
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
    fun assignVersion_auto(info: TestInfo) = inTransaction(db, info) {
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
    fun assignVersion_manual(info: TestInfo) = inTransaction(db, info) {
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
    fun embedded_eq(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val query = QueryDsl.insert(r).values {
            r.employeeId eq 99
            r.info1 eq RobotInfo1(9999, "ABC")
            r.info2 eq null
            r.addressId eq 1
            r.departmentId eq 1
            r.version eq 0
        }
        val (count, key) = db.runQuery(query)
        assertEquals(1, count)
        assertNull(key)
        val sql = query.dryRun(db.config)
        assertTrue(sql.sql.contains("hiredate"))
        assertTrue(sql.sql.contains("salary"))
    }

    @Test
    fun embedded_eq_partial_null(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val query = QueryDsl.insert(r).values {
            r.employeeId eq 99
            r.info1 eq RobotInfo1(9999, "ABC")
            r.info2 eq RobotInfo2(null, BigDecimal(3_000))
            r.addressId eq 1
            r.departmentId eq 1
            r.version eq 0
        }
        val (count, key) = db.runQuery(query)
        assertEquals(1, count)
        assertNull(key)
        val sql = query.dryRun(db.config)
        assertTrue(sql.sql.contains("hiredate"))
        assertTrue(sql.sql.contains("salary"))
    }

    @Test
    fun embedded_eqIfNotNull(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val query = QueryDsl.insert(r).values {
            r.employeeId eq 99
            r.info1 eq RobotInfo1(9999, "ABC")
            r.info2 eqIfNotNull null
            r.addressId eq 1
            r.departmentId eq 1
            r.version eq 0
        }
        val (count, key) = db.runQuery(query)
        assertEquals(1, count)
        assertNull(key)
        val sql = query.dryRun(db.config)
        assertFalse(sql.sql.contains("hiredate"))
        assertFalse(sql.sql.contains("salary"))
    }

    @Test
    fun embedded_eqIfNotNull_partial_null(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val query = QueryDsl.insert(r).values {
            r.employeeId eq 99
            r.info1 eq RobotInfo1(9999, "ABC")
            r.info2 eqIfNotNull RobotInfo2(null, BigDecimal(3_000))
            r.addressId eq 1
            r.departmentId eq 1
            r.version eq 0
        }
        val (count, key) = db.runQuery(query)
        assertEquals(1, count)
        assertNull(key)
        val sql = query.dryRun(db.config)
        assertFalse(sql.sql.contains("hiredate"))
        assertTrue(sql.sql.contains("salary"))
    }
}
