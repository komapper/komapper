package integration.jdbc

import integration.Address
import integration.Employee
import integration.meta
import kotlinx.coroutines.flow.count
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.operator.asc
import org.komapper.core.dsl.operator.ascNullsFirst
import org.komapper.core.dsl.operator.ascNullsLast
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.descNullsFirst
import org.komapper.core.dsl.operator.descNullsLast
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: JdbcDatabase) {

    @Test
    fun list() {
        val a = Address.meta
        val list: List<Address> = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(list)
    }

    @Test
    fun first() {
        val a = Address.meta
        val address: Address = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.meta
        val address: Address? = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun collect() {
        val a = Address.meta
        val count = db.runQuery {
            EntityDsl.from(a).collect { it.count() }
        }
        assertEquals(15, count)
    }

    @Test
    fun decoupling() {
        val a = Address.meta
        val query = EntityDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val list = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun orderBy() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(1, list.first().employeeId)
        assertEquals(14, list.last().employeeId)
    }

    @Test
    fun orderBy_asc() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.employeeId.asc())
        val list = db.runQuery { query }
        assertEquals(1, list.first().employeeId)
        assertEquals(14, list.last().employeeId)
    }

    @Test
    fun orderBy_desc() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(14, list.first().employeeId)
        assertEquals(1, list.last().employeeId)
    }

    @Test
    fun orderBy_ascNullsFirst() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.managerId.ascNullsFirst())
        val list = db.runQuery { query }
        assertNull(list.first().managerId)
        assertEquals(13, list.last().managerId)
    }

    @Test
    fun orderBy_ascNullsLast() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.managerId.ascNullsLast())
        val list = db.runQuery { query }
        assertEquals(4, list.first().managerId)
        assertNull(list.last().managerId)
    }

    @Test
    fun orderBy_descNullsFirst() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.managerId.descNullsFirst())
        val list = db.runQuery { query }
        assertNull(list.first().managerId)
        assertEquals(4, list.last().managerId)
    }

    @Test
    fun orderBy_descNullsLast() {
        val e = Employee.meta
        val query = EntityDsl.from(e).orderBy(e.managerId.descNullsLast())
        val list = db.runQuery { query }
        assertEquals(13, list.first().managerId)
        assertNull(list.last().managerId)
    }
}
