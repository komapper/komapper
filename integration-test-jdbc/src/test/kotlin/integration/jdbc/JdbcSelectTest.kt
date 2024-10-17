package integration.jdbc

import integration.core.Address
import integration.core.Android
import integration.core.Dbms
import integration.core.Robot
import integration.core.RobotInfo1
import integration.core.RobotInfo2
import integration.core.Run
import integration.core.address
import integration.core.android
import integration.core.employee
import integration.core.location
import integration.core.robot
import kotlinx.coroutines.flow.count
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.metamodel.define
import org.komapper.core.dsl.operator.case
import org.komapper.core.dsl.operator.columnExpression
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.core.dsl.query.single
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcSelectTest(private val db: JdbcDatabase) {
    @Test
    fun select() {
        val result = db.runQuery {
            QueryDsl.select(literal("hello")).single()
        }
        assertEquals("hello", result)
    }

    @Test
    fun select_scalar() {
        val result = db.runQuery {
            QueryDsl.select(max(literal(1)))
        }
        assertEquals(1, result)
    }

    @Test
    fun list() {
        val a = Meta.address
        val list: List<Address> = db.runQuery {
            QueryDsl.from(a)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun first() {
        val a = Meta.address
        val address: Address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Meta.address
        val address: Address? = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun single() {
        val a = Meta.address
        val address: Address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 1 }.single()
        }
        assertNotNull(address)
    }

    @Test
    fun singleOrNull() {
        val a = Meta.address
        val address: Address? = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 99 }.singleOrNull()
        }
        assertNull(address)
    }

    @Test
    fun collect() {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.from(a).collect { it.count() }
        }
        assertEquals(15, count)
    }

    @Test
    fun decoupling() {
        val a = Meta.address
        val query = QueryDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val list = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1),
            ),
            list,
        )
    }

    @Test
    fun option() {
        val e = Meta.employee
        val emp = db.runQuery {
            QueryDsl.from(e)
                .options {
                    it.copy(
                        fetchSize = 10,
                        maxRows = 100,
                        queryTimeoutSeconds = 1000,
                        allowMissingWhereClause = true,
                    )
                }
                .where {
                    e.employeeId eq 1
                }.first()
        }
        println(emp)
    }

    @Test
    fun caseExpression() {
        val a = Meta.address
        val caseExpression = case(
            When(
                {
                    a.street eq "STREET 2"
                    a.addressId greater 1
                },
                literal("HIT"),
            ),
        ) { literal("NO HIT") }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "NO HIT"),
            list,
        )
    }

    @Test
    fun caseExpression_multipleWhen() {
        val a = Meta.address
        val caseExpression = case(
            When(
                {
                    a.street eq "STREET 2"
                    a.addressId greater 1
                },
                literal("HIT"),
            ),
            When(
                { a.street eq "STREET 3" },
                concat(a.street, "!!!"),
            ),
        ) { literal("NO HIT") }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "STREET 3!!!"),
            list,
        )
    }

    @Test
    fun caseExpression_otherwiseNotSpecified() {
        val a = Meta.address
        val caseExpression = case(
            When(
                {
                    a.street eq "STREET 2"
                    a.addressId greater 1
                },
                literal("HIT"),
            ),
        )
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to null, "STREET 2" to "HIT", "STREET 3" to null),
            list,
        )
    }

    @Test
    fun defaultWhere() {
        val a = Meta.address.define { a ->
            where { a.addressId eq 1 }
        }
        val list = db.runQuery { QueryDsl.from(a) }
        assertEquals(1, list.size)
    }

    @Test
    fun defaultWhere_join() {
        val e = Meta.employee
        val a = Meta.address.define { a ->
            where { a.addressId eq 1 }
        }
        val list = db.runQuery {
            QueryDsl.from(e).innerJoin(a) {
                e.addressId eq a.addressId
            }
        }
        assertEquals(1, list.size)
    }

    @Test
    fun defaultWhere_update_single() {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        val a2 = Meta.address.define { a2 ->
            where { a2.version eq 99 }
        }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a2).single(address) }.run { }
        }
    }

    @Test
    fun defaultWhere_update_set() {
        val a = Meta.address.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.update(a).set { a.street eq "hello" } }
        assertEquals(1, count)
    }

    @Test
    fun defaultWhere_delete_single() {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        val a2 = Meta.address.define { a2 ->
            where { a2.version eq 99 }
        }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.delete(a2).single(address) }
        }
    }

    @Test
    fun defaultWhere_delete_all() {
        val a = Meta.address.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.delete(a).all() }
        assertEquals(1, count)
    }

    @Test
    fun embedded() {
        val r = Meta.robot
        val list: List<Robot> = db.runQuery {
            QueryDsl.from(r).where { r.info1 eq RobotInfo1(7839, "KING") }
        }
        assertEquals(1, list.size)
        assertEquals(9, list[0].employeeId)
    }

    @Test
    fun embedded_null() {
        val r = Meta.robot
        val list: List<Robot> = db.runQuery {
            QueryDsl.from(r).where { r.info2 eq RobotInfo2(salary = BigDecimal(3000)) }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(8, 13), list.map { it.employeeId })
    }

    @Test
    fun embedded_generics() {
        val a = Meta.android
        val list: List<Android> = db.runQuery {
            QueryDsl.from(a).where { a.info1 eq (7839 to "KING") }
        }
        assertEquals(1, list.size)
        assertEquals(9, list[0].employeeId)
    }

    @Test
    fun embedded_generics_null() {
        val a = Meta.android
        val list: List<Android> = db.runQuery {
            QueryDsl.from(a).where { a.info2 eq (null to BigDecimal(3000)) }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(8, 13), list.map { it.employeeId })
    }

    @Test
    fun `use typealias`() {
        val a = Meta.location
        val list = db.runQuery {
            QueryDsl.from(a)
        }
        assertEquals(15, list.size)
    }

    @Test
    @Run(onlyIf = [Dbms.MYSQL])
    fun simpleArgument() {
        val value = db.runQuery {
            QueryDsl.select(fromUnixTime(1447430881L)).single()
        }
        assertEquals(LocalDateTime.of(2015, 11, 13, 16, 8, 1), value)
    }

    private fun fromUnixTime(value: Long): ColumnExpression<LocalDateTime, LocalDateTime> {
        val name = "fromUnixTime"
        val o1 = Operand.simpleArgument(value)
        return columnExpression(name, listOf(o1)) {
            append("FROM_UNIXTIME(")
            visit(o1)
            append(")")
        }
    }

    @Test
    fun options() {
        val myDsl = QueryDsl(selectOptions = SelectOptions(allowMissingWhereClause = false))
        val e = assertFailsWith<IllegalStateException> {
            db.runQuery {
                myDsl.select(literal("hello")).single()
            }
            Unit
        }
        println(e)
    }
}
