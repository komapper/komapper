package integration.r2dbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.avg
import org.komapper.core.dsl.count
import org.komapper.core.dsl.max
import org.komapper.core.dsl.min
import org.komapper.core.dsl.sum
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQueryAggregateTest(private val db: R2dbcDatabase) {

    @Test
    fun aggregate_avg() = inTransaction(db) {
        val a = Address.meta
        val avg = db.runQuery {
            SqlDsl.from(a).select(avg(a.addressId)).first()
        }
        assertEquals(8.0, avg!!, 0.0)
    }

    @Test
    fun aggregate_countAsterisk() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            SqlDsl.from(a).select(count()).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_count() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            SqlDsl.from(a).select(count(a.street)).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_sum() = inTransaction(db) {
        val a = Address.meta
        val sum = db.runQuery { SqlDsl.from(a).select(sum(a.addressId)).first() }
        assertEquals(120, sum)
    }

    @Test
    fun aggregate_max() = inTransaction(db) {
        val a = Address.meta
        val max = db.runQuery { SqlDsl.from(a).select(max(a.addressId)).first() }
        assertEquals(15, max)
    }

    @Test
    fun aggregate_min() = inTransaction(db) {
        val a = Address.meta
        val min = db.runQuery { SqlDsl.from(a).select(min(a.addressId)).first() }
        assertEquals(1, min)
    }

    @Test
    fun having() = inTransaction(db) {
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(e)
                .groupBy(e.departmentId)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }.toList()
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

    @Test
    fun having_empty_groupBy() = inTransaction(db) {
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(e)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }.toList()
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }
}
