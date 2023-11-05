package integration.jdbc

import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.avg
import org.komapper.core.dsl.operator.cumeDist
import org.komapper.core.dsl.operator.denseRank
import org.komapper.core.dsl.operator.min
import org.komapper.core.dsl.operator.over
import org.komapper.core.dsl.operator.percentRank
import org.komapper.core.dsl.operator.rank
import org.komapper.core.dsl.operator.rowNumber
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcSelectWindowFunctionTest(private val db: JdbcDatabase) {

    @Test
    fun testPartitionBy() {
        val e = Meta.employee
        val averageSalary = avg(e.salary).over { partitionBy(e.departmentId) }

        val v = QueryDsl.from(e).select(
            e.departmentId,
            e.employeeName,
            e.salary,
            averageSalary,
        )

        val query = QueryDsl.from(v)
            .where {
                @Suppress("UNCHECKED_CAST")
                v[e.salary] greater (v[averageSalary] as ColumnExpression<BigDecimal, BigDecimal>)
            }
            .orderBy(v[e.departmentId], v[e.employeeName])
            .select(
                v[e.departmentId],
                v[e.employeeName],
                v[e.salary],
                v[averageSalary],
            )

        val list = db.runQuery { query }
        println(list)
        assertEquals(6, list.size)
        list[0].let {
            assertEquals(1, it[v[e.departmentId]])
            assertEquals("KING", it[v[e.employeeName]])
        }
        list[5].let {
            assertEquals(3, it[v[e.departmentId]])
            assertEquals("BLAKE", it[v[e.employeeName]])
        }
        for (each in list) {
            assertTrue(each[v[e.salary]]!!.toDouble() > each[v[averageSalary]]!!)
        }
    }

    @Test
    fun testRowsBetween() {
        val e = Meta.employee

        val currentDate = e.hiredate alias "currentDate"
        val latestDate = min(e.hiredate).over {
            orderBy(e.hiredate)
            rowsBetween(preceding(1), preceding(1))
        } alias "latestDate"

        val pairs = db.runQuery {
            QueryDsl.from(e).select(currentDate, latestDate)
        }
        assertEquals(14, pairs.size)
        pairs[0].let {
            assertEquals(LocalDate.of(1980, 12, 17), it.first)
            assertNull(it.second)
        }
        pairs[1].let {
            assertEquals(LocalDate.of(1981, 2, 20), it.first)
            assertEquals(LocalDate.of(1980, 12, 17), it.second)
        }
    }

    @Test
    fun testRowNumber() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .selectNotNull(e.departmentId, rowNumber().over { orderBy(e.departmentId) })
        }
        val expected = listOf(
            (1 to 1L),
            (1 to 2L),
            (1 to 3L),
            (2 to 4L),
            (2 to 5L),
            (2 to 6L),
            (2 to 7L),
            (2 to 8L),
            (3 to 9L),
            (3 to 10L),
            (3 to 11L),
            (3 to 12L),
            (3 to 13L),
            (3 to 14L),
        )
        assertEquals(expected, list)
    }

    @Test
    fun testRank() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .selectNotNull(e.departmentId, rank().over { orderBy(e.departmentId) })
        }
        val expected = listOf(
            (1 to 1L),
            (1 to 1L),
            (1 to 1L),
            (2 to 4L),
            (2 to 4L),
            (2 to 4L),
            (2 to 4L),
            (2 to 4L),
            (3 to 9L),
            (3 to 9L),
            (3 to 9L),
            (3 to 9L),
            (3 to 9L),
            (3 to 9L),
        )
        assertEquals(expected, list)
    }

    @Test
    fun testDenseRank() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .selectNotNull(e.departmentId, denseRank().over { orderBy(e.departmentId) })
        }
        val expected = listOf(
            (1 to 1L),
            (1 to 1L),
            (1 to 1L),
            (2 to 2L),
            (2 to 2L),
            (2 to 2L),
            (2 to 2L),
            (2 to 2L),
            (3 to 3L),
            (3 to 3L),
            (3 to 3L),
            (3 to 3L),
            (3 to 3L),
            (3 to 3L),
        )
        assertEquals(expected, list)
    }

    @Test
    fun testPercentRank() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .selectNotNull(e.departmentId, percentRank().over { orderBy(e.departmentId) })
        }
        println(list)
    }

    @Test
    fun testCumeDist() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .selectNotNull(e.departmentId, cumeDist().over { orderBy(e.departmentId) })
        }
        println(list)
    }
}
