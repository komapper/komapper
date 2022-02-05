package integration.jdbc

import integration.core.department
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SelectDistinctTest(private val db: JdbcDatabase) {

    @Test
    fun distinct() {
        val d = Meta.department
        val e = Meta.employee
        val query = QueryDsl.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.runQuery { query }
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.runQuery { query2 }
        assertEquals(3, list2.size)
    }
}
