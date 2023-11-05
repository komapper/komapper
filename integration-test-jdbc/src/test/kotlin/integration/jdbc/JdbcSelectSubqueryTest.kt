package integration.jdbc

import integration.core.department
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.operator.sum
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcSelectSubqueryTest(private val db: JdbcDatabase) {

    @Test
    fun subquery_selectClause() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val query = QueryDsl.from(d)
            .orderBy(d.departmentId)
            .select(d.departmentName, subquery)
        val list = db.runQuery { query }
        val expected = listOf("ACCOUNTING" to 3L, "RESEARCH" to 5L, "SALES" to 6L, "OPERATIONS" to 0L)
        assertEquals(expected, list)
    }

    @Test
    fun subquery_selectClause_notNull() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.selectNotNull(count())
        val query = QueryDsl.from(d)
            .orderBy(d.departmentId)
            .selectNotNull(d.departmentName, subquery)
        val list = db.runQuery { query }
        val expected = listOf("ACCOUNTING" to 3L, "RESEARCH" to 5L, "SALES" to 6L, "OPERATIONS" to 0L)
        assertEquals(expected, list)
    }

    @Test
    fun subquery_whereClause() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(d)
            .where {
                d.departmentName eq "SALES"
            }.select(max(d.departmentId))
        val query = QueryDsl.from(e).where {
            e.departmentId eq subquery
        }
        val list = db.runQuery { query }
        assertEquals(6, list.size)
    }

    @Test
    fun subquery_as_inlineView_property() {
        val d = Meta.department
        val e = Meta.employee
        val v = QueryDsl.from(e)
            .innerJoin(d) { e.departmentId eq d.departmentId }
            .groupBy(d.departmentName)
            .select(d.departmentName, sum(e.salary))
        val query = QueryDsl.from(v).orderBy(v[d.departmentName])
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        list[0].let { record ->
            assertEquals("ACCOUNTING", record[v[d.departmentName]])
            assertEqualsBigDecimal(BigDecimal("8750.00"), record[v[sum(e.salary)]])
        }
        list[1].let { record ->
            assertEquals("RESEARCH", record[v[d.departmentName]])
            assertEqualsBigDecimal(BigDecimal("10875.00"), record[v[sum(e.salary)]])
        }
        list[2].let { record ->
            assertEquals("SALES", record[v[d.departmentName]])
            assertEqualsBigDecimal(BigDecimal("9400.00"), record[v[sum(e.salary)]])
        }
    }

    @Test
    fun subquery_as_inlineView_alias() {
        val d = Meta.department
        val e = Meta.employee
        val v = QueryDsl.from(e)
            .innerJoin(d) { e.departmentId eq d.departmentId }
            .groupBy(d.departmentName)
            .select(d.departmentName alias "a", sum(e.salary) alias "b")
        val query = QueryDsl.from(v).orderBy(v[d.departmentName alias "a"])
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        list[0].let { record ->
            assertEquals("ACCOUNTING", record[v[d.departmentName alias "a"]])
            assertEqualsBigDecimal(BigDecimal("8750.00"), record[v[sum(e.salary) alias "b"]])
        }
        list[1].let { record ->
            assertEquals("RESEARCH", record[v[d.departmentName alias "a"]])
            assertEqualsBigDecimal(BigDecimal("10875.00"), record[v[sum(e.salary) alias "b"]])
        }
        list[2].let { record ->
            assertEquals("SALES", record[v[d.departmentName alias "a"]])
            assertEqualsBigDecimal(BigDecimal("9400.00"), record[v[sum(e.salary) alias "b"]])
        }
    }

    @Test
    fun subquery_as_inlineView_union() {
        val d = Meta.department
        val e = Meta.employee

        val q1 =
            QueryDsl.from(e).where { e.employeeId eq 1 }
                .select(e.employeeId alias "ID", e.employeeName alias "NAME")
        val q2 = QueryDsl.from(d).where { d.departmentId eq 3 }
            .select(d.departmentId alias "ID", d.departmentName alias "NAME")
        val q3 = q1.union(q2)

        val query = QueryDsl.from(q3)
            .innerJoin(e) { e.employeeId eq q3[e.employeeId alias "ID"] }
            .selectAsRecord(q3[e.employeeId alias "ID"], q3[e.employeeName alias "NAME"], e.salary)

        val list = db.runQuery { query }
        assertEquals(2, list.size)

        list[0].let { record ->
            assertEquals(1, record[q3[e.employeeId alias "ID"]])
            assertEquals("SMITH", record[q3[e.employeeName alias "NAME"]])
            assertEqualsBigDecimal(BigDecimal("800.00"), record[e.salary])
        }
        list[1].let { record ->
            assertEquals(3, record[q3[e.employeeId alias "ID"]])
            assertEquals("SALES", record[q3[e.employeeName alias "NAME"]])
            assertEqualsBigDecimal(BigDecimal("1250.00"), record[e.salary])
        }
    }

    private fun assertEqualsBigDecimal(expected: BigDecimal, actual: BigDecimal?) {
        assertEquals(0, expected.compareTo(actual))
    }
}
