package integration.jdbc

import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.avg
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.min
import org.komapper.core.dsl.operator.over
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
            .orderBy(v[e.departmentId])
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
                .orderBy(e.employeeId)
                .selectNotNull(e.employeeId, rowNumber().over { orderBy(e.employeeId.desc()) })
        }
        val numbers = 1..14
        val expected = numbers.zip(numbers.reversed().map { it.toLong() })
        assertEquals(expected, list)
    }
}
