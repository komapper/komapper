package integration.jdbc

import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.asc
import org.komapper.core.dsl.operator.ascNullsFirst
import org.komapper.core.dsl.operator.ascNullsLast
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.descNullsFirst
import org.komapper.core.dsl.operator.descNullsLast
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcSelectOrderByTest(private val db: JdbcDatabase) {

    @Test
    fun orderBy() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(1, list.first().employeeId)
        assertEquals(14, list.last().employeeId)
    }

    @Test
    fun orderBy_asc() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.employeeId.asc())
        val list = db.runQuery { query }
        assertEquals(1, list.first().employeeId)
        assertEquals(14, list.last().employeeId)
    }

    @Test
    fun orderBy_desc() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(14, list.first().employeeId)
        assertEquals(1, list.last().employeeId)
    }

    @Test
    fun orderBy_ascNullsFirst() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.managerId.ascNullsFirst())
        val list = db.runQuery { query }
        assertNull(list.first().managerId)
        assertEquals(13, list.last().managerId)
    }

    @Test
    fun orderBy_ascNullsLast() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.managerId.ascNullsLast())
        val list = db.runQuery { query }
        assertEquals(4, list.first().managerId)
        assertNull(list.last().managerId)
    }

    @Test
    fun orderBy_descNullsFirst() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.managerId.descNullsFirst())
        val list = db.runQuery { query }
        assertNull(list.first().managerId)
        assertEquals(4, list.last().managerId)
    }

    @Test
    fun orderBy_descNullsLast() {
        val e = Meta.employee
        val query = QueryDsl.from(e).orderBy(e.managerId.descNullsLast())
        val list = db.runQuery { query }
        assertEquals(13, list.first().managerId)
        assertNull(list.last().managerId)
    }
}
