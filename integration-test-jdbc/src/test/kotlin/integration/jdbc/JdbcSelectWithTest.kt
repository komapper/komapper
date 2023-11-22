package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.department
import integration.core.employee
import integration.core.t
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.operator.sum
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
@Run(unless = [Dbms.MYSQL_5])
class JdbcSelectWithTest(private val db: JdbcDatabase) {

    @Test
    fun with() {
        val e = Meta.employee
        val d = Meta.department
        val sales = d.clone(table = "sales")
        val subquery = QueryDsl.from(d).where { d.departmentName eq "SALES" }
        val query = QueryDsl.with(sales, subquery)
            .from(sales)
            .innerJoin(e) { sales.departmentId eq e.departmentId }
            .orderBy(e.employeeName)
            .select(sales.departmentName, e.employeeName)
        val list = db.runQuery(query)
        assertEquals(
            listOf(
                "SALES" to "ALLEN",
                "SALES" to "BLAKE",
                "SALES" to "JAMES",
                "SALES" to "MARTIN",
                "SALES" to "TURNER",
                "SALES" to "WARD",
            ),
            list,
        )
    }

    @Run(unless = [Dbms.H2])
    @Test
    fun with_multiple() {
        val e = Meta.employee
        val d = Meta.department
        val accounting = d.clone(table = "accounting")
        val manager = e.clone(table = "manager")

        val accountingSubquery = QueryDsl.from(d).where { d.departmentName eq "ACCOUNTING" }
        val managerSubquery = QueryDsl.from(e)
            .innerJoin(accounting) { e.departmentId eq accounting.departmentId }
            .where { e.managerId.isNull() }

        val query = QueryDsl.with(accounting to accountingSubquery, manager to managerSubquery)
            .from(manager)
            .orderBy(manager.employeeName)
            .select(manager.employeeName)

        val list = db.runQuery(query)
        assertEquals(listOf("KING"), list)
    }

    @Test
    fun withRecursive() {
        val t = Meta.t
        val subquery =
            QueryDsl.select(literal(1)).unionAll(
                QueryDsl.from(t).where { t.n less 10 }.select(t.n + 1),
            )
        val query = QueryDsl.withRecursive(t, subquery).from(t).select(sum(t.n))
        val result = db.runQuery(query)
        assertEquals(55, result)
    }
}
