package integration.r2dbc

import integration.department
import integration.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: R2dbcDatabase) {

    @Test
    fun distinct() = inTransaction(db) {
        val d = Meta.department
        val e = Meta.employee
        val query = QueryDsl.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.runQuery { query }.toList()
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.runQuery { query2 }.toList()
        assertEquals(3, list2.size)
    }
}
