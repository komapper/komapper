package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: R2dbcDatabase) {

    @Test
    fun distinct() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val query = R2dbcSqlDsl.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.runQuery { query }.toList()
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.runQuery { query2 }.toList()
        assertEquals(3, list2.size)
    }
}
