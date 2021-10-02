package integration.jdbc

import integration.Department
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: JdbcDatabase) {

    @Test
    fun distinct() {
        val d = Department.meta
        val e = Employee.meta
        val query = SqlDsl.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.runQuery { query }
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.runQuery { query2 }
        assertEquals(3, list2.size)
    }
}
