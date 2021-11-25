package integration.r2dbc

import integration.address
import integration.department
import integration.employee
import integration.manager
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SelectJoinTest(private val db: R2dbcDatabase) {

    @Test
    fun innerJoin() = inTransaction(db) {
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
    fun leftJoin() = inTransaction(db) {
        val a = Meta.address
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }

    @Test
    fun innerJoin_multiConditions() = inTransaction(db) {
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
    fun include_no_association() = inTransaction(db) {
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

        assertTrue(!store.contains(a, e))
        assertTrue(!store.contains(a, d))
        assertTrue(!store.contains(e, a))
        assertTrue(!store.contains(e, d))
        assertTrue(!store.contains(d, a))
        assertTrue(!store.contains(d, e))

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isEmpty())
        assertTrue(store.oneToMany(e, a).isEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_one_association() = inTransaction(db) {
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

        assertTrue(store.contains(a, e))
        assertTrue(!store.contains(a, d))
        assertTrue(store.contains(e, a))
        assertTrue(!store.contains(e, d))
        assertTrue(!store.contains(d, a))
        assertTrue(!store.contains(d, e))

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_two_associations() = inTransaction(db) {
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

        assertTrue(store.contains(a, e))
        assertTrue(store.contains(a, d))
        assertTrue(store.contains(e, a))
        assertTrue(store.contains(e, d))
        assertTrue(store.contains(d, a))
        assertTrue(store.contains(d, e))

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun includeAll() = inTransaction(db) {
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

        assertTrue(store.contains(a, e))
        assertTrue(store.contains(a, d))
        assertTrue(store.contains(e, a))
        assertTrue(store.contains(e, d))
        assertTrue(store.contains(d, a))
        assertTrue(store.contains(d, e))

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun oneToMany() = inTransaction(db) {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        assertTrue(store.contains(d, e))
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
    fun oneToManyById() = inTransaction(db) {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        assertTrue(store.contains(d, e))
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
    fun oneToOne() = inTransaction(db) {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        assertTrue(store.contains(e, a))
        val map = store.oneToOne(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map.filterKeys { it.employeeId == 1 }.values.first()
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun oneToOneById() = inTransaction(db) {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        assertTrue(store.contains(e, a))
        val map = store.oneToOneById(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map[1]
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun mainEntities() = inTransaction(db) {
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
        val employees = store.mainEntities
        assertEquals(14, employees.size)
    }

    @Test
    fun oneToMany_selfJoin() = inTransaction(db) {
        val e = Meta.employee
        val m = Meta.manager
        val store = db.runQuery {
            QueryDsl.from(m).innerJoin(e) {
                m.employeeId eq e.managerId
            }.includeAll()
        }

        val managers = store.mainEntities
        assertEquals(6, managers.size)
        assertEquals(managers.map { it.employeeId }.toSet(), setOf(4, 6, 7, 8, 9, 13))

        assertTrue(store.contains(m, e))
        val oneToMany = store.oneToMany(m, e)
        assertTrue(oneToMany.keys.containsAll(managers))
        assertTrue(managers.containsAll(oneToMany.keys))
    }
}
