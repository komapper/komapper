package integration.jdbc

import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.avg
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.countDistinct
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.operator.min
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.operator.sum
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcSelectAggregateTest(private val db: JdbcDatabase) {
    @Test
    fun aggregate_avg() {
        val a = Meta.address
        val avg = db.runQuery {
            QueryDsl.from(a).select(avg(a.addressId))
        }
        assertEquals(8.0, avg!!, 0.0)
    }

    @Test
    fun aggregate_countAsterisk() {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.from(a).select(count())
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_count() {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.from(a).select(count(a.street))
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_count_distinct() {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.from(e).select(countDistinct(e.managerId))
        }
        assertEquals(6, count)
    }

    @Test
    fun aggregate_sum() {
        val a = Meta.address
        val sum = db.runQuery { QueryDsl.from(a).select(sum(a.addressId)) }
        assertEquals(120, sum)
    }

    @Test
    fun aggregate_sum_plus_value() {
        val a = Meta.address
        val sum = db.runQuery { QueryDsl.from(a).select(sum(a.addressId) + 1) }
        assertEquals(121, sum)
    }

    @Test
    fun aggregate_sum_plus_right_hand_side_literal() {
        val a = Meta.address
        val sum = db.runQuery { QueryDsl.from(a).select(sum(a.addressId) + literal(1)) }
        assertEquals(121, sum)
    }

    @Test
    fun aggregate_sum_plus_left_hand_side_literal() {
        val a = Meta.address
        val sum = db.runQuery { QueryDsl.from(a).select(literal(1) + sum(a.addressId)) }
        assertEquals(121, sum)
    }

    @Test
    fun aggregate_sum_alias() {
        val a = Meta.address
        val sum = db.runQuery { QueryDsl.from(a).select(sum(a.addressId) alias "sum") }
        assertEquals(120, sum)
    }

    @Test
    fun aggregate_max() {
        val a = Meta.address
        val max = db.runQuery { QueryDsl.from(a).select(max(a.addressId)) }
        assertEquals(15, max)
    }

    @Test
    fun aggregate_min() {
        val a = Meta.address
        val min = db.runQuery { QueryDsl.from(a).select(min(a.addressId)) }
        assertEquals(1, min)
    }

    @Test
    fun having() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .groupBy(e.departmentId)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

    @Test
    fun `The aggregate function is in the select list and the having clause, but there is no groupBy clause`() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

    @Test
    fun `The aggregate function is in the having clause, but there is no groupBy clause`() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId)
        }
        assertEquals(listOf(2, 3), list)
    }

    @Test
    fun `The aggregate function is in the select list, but there is no groupBy clause`() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(1 to 3L, 2 to 5L, 3 to 6L), list)
    }
}
