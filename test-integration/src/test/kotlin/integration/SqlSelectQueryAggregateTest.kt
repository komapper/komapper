package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.avg
import org.komapper.core.dsl.count
import org.komapper.core.dsl.max
import org.komapper.core.dsl.min
import org.komapper.core.dsl.runQuery
import org.komapper.core.dsl.sum

@ExtendWith(Env::class)
class SqlSelectQueryAggregateTest(private val db: Database) {

    @Test
    fun aggregate_avg() {
        val a = Address.alias
        val avg = db.runQuery {
            SqlQuery.from(a).select(avg(a.addressId)).first()
        }
        assertEquals(8.0, avg!!, 0.0)
    }

    @Test
    fun aggregate_countAsterisk() {
        val a = Address.alias
        val count = db.runQuery {
            SqlQuery.from(a).select(count()).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_count() {
        val a = Address.alias
        val count = db.runQuery {
            SqlQuery.from(a).select(count(a.street)).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_sum() {
        val a = Address.alias
        val sum = db.runQuery { SqlQuery.from(a).select(sum(a.addressId)).first() }
        assertEquals(120, sum)
    }

    @Test
    fun aggregate_max() {
        val a = Address.alias
        val max = db.runQuery { SqlQuery.from(a).select(max(a.addressId)).first() }
        assertEquals(15, max)
    }

    @Test
    fun aggregate_min() {
        val a = Address.alias
        val min = db.runQuery { SqlQuery.from(a).select(min(a.addressId)).first() }
        assertEquals(1, min)
    }

    @Test
    fun having() {
        val e = Employee.alias
        val list = db.runQuery {
            SqlQuery.from(e)
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
    fun having_empty_groupBy() {
        val e = Employee.alias
        val list = db.runQuery {
            SqlQuery.from(e)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }
}
