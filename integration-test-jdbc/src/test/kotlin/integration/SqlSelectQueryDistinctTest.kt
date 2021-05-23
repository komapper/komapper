package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.SqlDsl

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: Database) {

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
