package integration.r2dbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import integration.newMeta
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class FlowQueryTest(val db: R2dbcDatabase) {

    @Test
    fun singleEntity() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2) }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleEntity_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a).where { a.addressId eq 1 }.union(
                SqlDsl.from(a).where { a.addressId eq 2 }
            ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun pairEntities() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            val e = Employee.meta
            SqlDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .innerJoin(e) { a.addressId eq e.addressId }
                .orderBy(a.addressId)
                .select(e)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.first.addressId })
    }

    @Test
    fun tripleEntities() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            val e = Employee.meta
            val d = Department.meta
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2) }
                .innerJoin(e) { a.addressId eq e.addressId }
                .innerJoin(d) { e.departmentId eq d.departmentId }
                .orderBy(a.addressId)
                .select(e, d)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.first.addressId })
    }

    @Test
    fun multipleEntities() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val m = Employee.newMeta()
        val d = Department.meta
        val flow = db.flow {
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2) }
                .innerJoin(e) { a.addressId eq e.addressId }
                .innerJoin(d) { e.departmentId eq d.departmentId }
                .leftJoin(m) { e.managerId eq m.employeeId }
                .orderBy(a.addressId)
                .select(e, d, m)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it[a]!!.addressId })
    }

    @Test
    fun singleColumn() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleColumn_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId).union(
                    SqlDsl.from(Address.meta)
                        .where { a.addressId eq 2 }
                        .select(a.addressId)
                ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun pairColumns() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2"
            ),
            flow.toList()
        )
    }

    @Test
    fun pairColumns_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street).union(
                    SqlDsl.from(Address.meta)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street)
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2"
            ),
            flow.toList()
        )
    }

    @Test
    fun tripleColumns() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun tripleColumns_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Address.meta
            SqlDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street, a.version).union(
                    SqlDsl.from(a)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street, a.version)
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun multipleColumns() = inTransaction(db) {
        val a = Address.meta
        val flow = db.flow {
            SqlDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, a.addressId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][a.addressId])
        assertEquals(2, list[1][a.addressId])
    }

    @Test
    fun multipleColumns_union() = inTransaction(db) {
        val e = Employee.meta
        val flow = db.flow {
            SqlDsl.from(e)
                .where { e.employeeId eq 1 }
                .select(e.employeeId, e.employeeNo, e.employeeName, e.salary).union(
                    SqlDsl.from(e)
                        .where { e.employeeId eq 2 }
                        .select(e.employeeId, e.employeeNo, e.employeeName, e.salary)
                ).orderBy(e.employeeId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][e.employeeId])
        assertEquals(2, list[1][e.employeeId])
    }

    @Test
    fun template() = inTransaction(db) {
        val flow = db.flow {
            TemplateDsl.from("select address_id from ADDRESS order by address_id").select { it.asInt("address_id") }
        }
        assertEquals((1..15).toList(), flow.toList())
    }
}
