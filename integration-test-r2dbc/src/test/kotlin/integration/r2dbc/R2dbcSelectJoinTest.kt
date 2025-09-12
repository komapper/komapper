package integration.r2dbc

import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.course
import integration.core.department
import integration.core.employee
import integration.core.manager
import integration.core.person
import integration.core.student
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcSelectJoinTest(private val db: R2dbcDatabase) {
    @Test
    fun innerJoin(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.MYSQL_5, Dbms.MYSQL])
    @Test
    fun fullJoin(info: TestInfo) = inTransaction(db, info) {
        val s = Meta.student
        val c = Meta.course
        val set = db.runQuery {
            QueryDsl.from(s).fullJoin(c) {
                s.studentId eq c.studentId
            }.select(s.studentName, c.courseName)
        }.toSet()
        assertEquals(5, set.size)
        assertEquals(
            setOf("taro" to "math", "taro" to "english", "hanako" to null, "jiro" to null, null to "science"),
            set
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.MYSQL_5, Dbms.MYSQL])
    @Test
    fun fullJoin_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
        val s = Meta.student
        val c = Meta.course
        val ex = assertThrows<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.from(s).fullJoin(c) {
                    s.studentId eq c.studentId
                }.select(s.studentName, c.courseName)
            }
            Unit
        }
        println(ex)
    }

    @Test
    fun innerJoin_multiConditions(info: TestInfo) = inTransaction(db, info) {
        val employee = Meta.employee
        val manager = Meta.manager
        val list = db.runQuery {
            QueryDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun include_no_association(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include()
        }

        assertTrue(store.contains(e))
        assertTrue(!store.contains(a))
        assertTrue(!store.contains(d))

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isEmpty())
        assertTrue(store.oneToMany(e, a).isEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_one_association(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a)
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d !in store)

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_two_associations(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a, d)
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun includeAll(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.includeAll()
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun oneToMany(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        val map = store.oneToMany(d, e)
        assertEquals(3, map.size)
        val employees1 = map.filterKeys { it.departmentId == 1 }.values.first()
        val employees2 = map.filterKeys { it.departmentId == 2 }.values.first()
        val employees3 = map.filterKeys { it.departmentId == 3 }.values.first()
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun oneToManyById(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        val map = store.oneToManyById(d, e)
        assertEquals(3, map.size)
        val employees1 = map[1]
        val employees2 = map[2]
        val employees3 = map[3]
        assertNotNull(employees1)
        assertNotNull(employees2)
        assertNotNull(employees3)
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun oneToOne(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        val map = store.oneToOne(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map.filterKeys { it.employeeId == 1 }.values.first()
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun oneToOneById(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        val map = store.oneToOneById(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map[1]
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun oneToMany_selfJoin(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val m = Meta.manager
        val store = db.runQuery {
            QueryDsl.from(m).innerJoin(e) {
                m.employeeId eq e.managerId
            }.includeAll()
        }

        val managers = store[m]
        assertEquals(6, managers.size)
        assertEquals(managers.map { it.employeeId }.toSet(), setOf(4, 6, 7, 8, 9, 13))

        val oneToMany = store.oneToMany(m, e)
        assertTrue(oneToMany.keys.containsAll(managers))
        assertTrue(managers.containsAll(oneToMany.keys))
    }

    @Test
    fun get(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.orderBy(d.departmentId).includeAll()
        }
        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)
        assertFalse(Meta.person in store)
        assertEquals(14, store[a].size)
        assertEquals(14, store[e].size)
        assertEquals(3, store[d].size)
    }
}
