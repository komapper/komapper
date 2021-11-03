package integration.jdbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.asc
import org.komapper.core.dsl.operator.ascNullsFirst
import org.komapper.core.dsl.operator.ascNullsLast
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.descNullsFirst
import org.komapper.core.dsl.operator.descNullsLast
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(Env::class)
class SqlSetOperationQueryTest(private val db: JdbcDatabase) {

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun except_entity() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId inList listOf(1, 2, 3, 4, 5) }
        val q2 = SqlDsl.from(e).where { e.employeeId inList listOf(2, 4, 6, 8) }
        val query = (q1 except q2).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        val e1 = list[0]
        val e2 = list[1]
        val e3 = list[2]
        assertEquals(1, e1.employeeId)
        assertEquals(3, e2.employeeId)
        assertEquals(5, e3.employeeId)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun intersect_entity() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId inList listOf(1, 2, 3, 4, 5) }
        val q2 = SqlDsl.from(e).where { e.employeeId inList listOf(2, 4, 6, 8) }
        val query = (q1 intersect q2).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(2, list.size)
        val e1 = list[0]
        val e2 = list[1]
        assertEquals(2, e1.employeeId)
        assertEquals(4, e2.employeeId)
    }

    @Test
    fun union_entity() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId eq 1 }
        val q2 = SqlDsl.from(e).where { e.employeeId eq 1 }
        val q3 = SqlDsl.from(e).where { e.employeeId eq 5 }
        val query = (q1 union q2 union q3).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(2, list.size)
        val e1 = list[0]
        val e2 = list[1]
        assertEquals(5, e1.employeeId)
        assertEquals(1, e2.employeeId)
    }

    @Test
    fun union_subquery() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId eq 1 }.select(e.employeeId)
        val q2 = SqlDsl.from(e).where { e.employeeId eq 6 }.select(e.employeeId)
        val subquery = q1 union q2
        val query = SqlDsl.from(e).where {
            e.managerId inList { subquery }
        }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun union_columns() {
        val e = Employee.meta
        val a = Address.meta
        val d = Department.meta
        val q1 =
            SqlDsl.from(e).where { e.employeeId eq 1 }
                .select(e.employeeId alias "ID", e.employeeName alias "NAME")
        val q2 = SqlDsl.from(a).where { a.addressId eq 2 }
            .select(a.addressId alias "ID", a.street alias "NAME")
        val q3 = SqlDsl.from(d).where { d.departmentId eq 3 }
            .select(d.departmentId alias "ID", d.departmentName alias "NAME")
        val query = (q1 union q2 union q3).orderBy("ID", desc("NAME"))
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        assertEquals(1 to "SMITH", list[0])
        assertEquals(2 to "STREET 2", list[1])
        assertEquals(3 to "SALES", list[2])
    }

    @Test
    fun unionAll_entity() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId eq 1 }
        val q2 = SqlDsl.from(e).where { e.employeeId eq 1 }
        val q3 = SqlDsl.from(e).where { e.employeeId eq 5 }
        val query = (q1 unionAll q2 unionAll q3).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        val e1 = list[0]
        val e2 = list[1]
        val e3 = list[2]
        assertEquals(5, e1.employeeId)
        assertEquals(1, e2.employeeId)
        assertEquals(1, e3.employeeId)
    }

    @Test
    fun emptyWhereClause() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).where { e.employeeId eq 1 }
        val q2 = SqlDsl.from(e)
        val query = (q1 union q2).options { it.copy(allowEmptyWhereClause = false) }
        val ex = assertFailsWith<IllegalStateException> {
            db.runQuery { query }.let { }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun orderBy() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.employeeId alias "ID")
        val query = (q1 union q1).orderBy("ID")
        val list = db.runQuery { query }
        println(list)
        assertEquals(1, list.first())
        assertEquals(14, list.last())
    }

    @Test
    fun orderBy_asc() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.employeeId alias "ID")
        val query = (q1 union q1).orderBy(asc("ID"))
        val list = db.runQuery { query }
        println(list)
        assertEquals(1, list.first())
        assertEquals(14, list.last())
    }

    @Test
    fun orderBy_desc() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.employeeId alias "ID")
        val query = (q1 union q1).orderBy(desc("ID"))
        val list = db.runQuery { query }
        println(list)
        assertEquals(14, list.first())
        assertEquals(1, list.last())
    }

    @Test
    fun orderBy_ascNullsFirst() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.managerId alias "ID")
        val query = (q1 union q1).orderBy(ascNullsFirst("ID"))
        val list = db.runQuery { query }
        println(list)
        assertNull(list.first())
        assertEquals(13, list.last())
    }

    @Test
    fun orderBy_ascNullsLast() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.managerId alias "ID")
        val query = (q1 union q1).orderBy(ascNullsLast("ID"))
        val list = db.runQuery { query }
        println(list)
        assertEquals(4, list.first())
        assertNull(list.last())
    }

    @Test
    fun orderBy_descNullsFirst() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.managerId alias "ID")
        val query = (q1 union q1).orderBy(descNullsFirst("ID"))
        val list = db.runQuery { query }
        println(list)
        assertNull(list.first())
        assertEquals(4, list.last())
    }

    @Test
    fun orderBy_descNullsLast() {
        val e = Employee.meta
        val q1 = SqlDsl.from(e).select(e.managerId alias "ID")
        val query = (q1 union q1).orderBy(descNullsLast("ID"))
        val list = db.runQuery { query }
        println(list)
        assertEquals(13, list.first())
        assertNull(list.last())
    }
}
