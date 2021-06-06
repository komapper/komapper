package integration.r2dbc

import integration.Department
import integration.Employee
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: R2dbcDatabase) {

    @Test
    fun distinct() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val query = SqlDsl.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.runQuery { query }.toList()
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.runQuery { query2 }.toList()
        assertEquals(3, list2.size)
    }
}
