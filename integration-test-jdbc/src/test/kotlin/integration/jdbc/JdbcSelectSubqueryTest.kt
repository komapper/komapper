package integration.jdbc

import integration.core.NameAndAmount
import integration.core.department
import integration.core.employee
import integration.core.nameAndAmount
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.operator.sum
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcSelectSubqueryTest(private val db: JdbcDatabase) {

    @Test
    fun subquery_selectClause() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val query = QueryDsl.from(d)
            .orderBy(d.departmentId)
            .select(d.departmentName, subquery)
        val list = db.runQuery { query }
        val expected = listOf("ACCOUNTING" to 3L, "RESEARCH" to 5L, "SALES" to 6L, "OPERATIONS" to 0L)
        assertEquals(expected, list)
    }

    @Test
    fun subquery_selectClause_notNull() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.selectNotNull(count())
        val query = QueryDsl.from(d)
            .orderBy(d.departmentId)
            .selectNotNull(d.departmentName, subquery)
        val list = db.runQuery { query }
        val expected = listOf("ACCOUNTING" to 3L, "RESEARCH" to 5L, "SALES" to 6L, "OPERATIONS" to 0L)
        assertEquals(expected, list)
    }

    @Test
    fun subquery_whereClause() {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(d)
            .where {
                d.departmentName eq "SALES"
            }.select(max(d.departmentId))
        val query = QueryDsl.from(e).where {
            e.departmentId eq subquery
        }
        val list = db.runQuery { query }
        assertEquals(6, list.size)
    }

    @Test
    fun subquery_as_derived_table() {
        val d = Meta.department
        val e = Meta.employee
        val t = Meta.nameAndAmount

        val subquery = QueryDsl.from(e)
            .innerJoin(d) { e.departmentId eq d.departmentId }
            .groupBy(d.departmentName)
            .select(d.departmentName, sum(e.salary))

        val query = QueryDsl.from(t, subquery).orderBy(t.name)
        val list = db.runQuery { query }
        val expected = listOf(
            NameAndAmount("ACCOUNTING", BigDecimal("8750.00")),
            NameAndAmount("RESEARCH", BigDecimal("10875.00")),
            NameAndAmount("SALES", BigDecimal("9400.00")),
        )
        assertEquals(expected, list)
    }

    @Test
    fun subquery_as_derived_table_union() {
        val t = Meta.nameAndAmount

        val q1 = QueryDsl.select(literal("one") alias "name", literal(BigDecimal.ONE) alias "amount")
        val q2 = QueryDsl.select(literal("ten") alias "name", literal(BigDecimal.TEN) alias "amount")
        val subquery = q1.union(q2)

        val query = QueryDsl.from(t, subquery).where { t.name eq "ten" }

        val list = db.runQuery { query }
        assertEquals(1, list.size)
        val expected = listOf(
            NameAndAmount("ten", BigDecimal.TEN),
        )
        assertEquals(expected, list)
    }
}
