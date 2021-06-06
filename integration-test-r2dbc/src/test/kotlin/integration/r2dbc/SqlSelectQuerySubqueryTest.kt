package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.count
import org.komapper.core.dsl.max
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQuerySubqueryTest(private val db: R2dbcDatabase) {

    @Test
    fun subquery_selectClause() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val subquery = SqlDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val query = SqlDsl.from(d)
            .orderBy(d.departmentId)
            .select(d.departmentName, subquery)
        val list = db.runQuery { query }.toList()
        val expected = listOf("ACCOUNTING" to 3L, "RESEARCH" to 5L, "SALES" to 6L, "OPERATIONS" to 0L)
        assertEquals(expected, list)
    }

    @Test
    fun subquery_whereClause() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val subquery = SqlDsl.from(d)
            .where {
                d.departmentName eq "SALES"
            }.select(max(d.departmentId))
        val query = SqlDsl.from(e).where {
            e.departmentId eq subquery
        }
        val list = db.runQuery { query }.toList()
        assertEquals(6, list.size)
    }
}
