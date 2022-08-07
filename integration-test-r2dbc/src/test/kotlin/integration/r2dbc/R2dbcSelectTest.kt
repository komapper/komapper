package integration.r2dbc

import integration.core.Address
import integration.core.Robot
import integration.core.RobotInfo1
import integration.core.RobotInfo2
import integration.core.address
import integration.core.employee
import integration.core.robot
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.metamodel.define
import org.komapper.core.dsl.operator.case
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.core.dsl.query.single
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(R2dbcEnv::class)
class R2dbcSelectTest(private val db: R2dbcDatabase) {

    @Test
    fun list(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val list: List<Address> = db.runQuery {
            QueryDsl.from(a)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun first(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address: Address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address: Address? = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun single(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address: Address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 1 }.single()
        }
        assertNotNull(address)
    }

    @Test
    fun singleOrNull(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address: Address? = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 99 }.singleOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val flow = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun option(info: TestInfo) = inTransaction(db, info) {
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
    fun caseExpression(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            )
        ) { literal("NO HIT") }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "NO HIT"),
            list
        )
    }

    @Test
    fun caseExpression_multipleWhen(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            ),
            When(
                { a.street eq "STREET 3" },
                concat(a.street, "!!!")
            )
        ) { literal("NO HIT") }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "STREET 3!!!"),
            list
        )
    }

    @Test
    fun caseExpression_otherwiseNotSpecified(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            )
        )
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to null, "STREET 2" to "HIT", "STREET 3" to null),
            list
        )
    }

    @Test
    fun defaultWhere(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address.define { a ->
            where { a.addressId eq 1 }
        }
        val list = db.runQuery { QueryDsl.from(a) }
        assertEquals(1, list.size)
    }

    @Test
    fun defaultWhere_update_single(info: TestInfo) = inTransaction(db, info) {
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
    fun defaultWhere_update_set(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.update(a).set { a.street eq "hello" } }
        assertEquals(1, count)
    }

    @Test
    fun defaultWhere_delete_single(info: TestInfo) = inTransaction(db, info) {
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
    fun defaultWhere_delete_all(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.delete(a).all() }
        assertEquals(1, count)
    }

    @Test
    fun embedded(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val list: List<Robot> = db.runQuery {
            QueryDsl.from(r).where { r.info1 eq RobotInfo1(7839, "KING") }
        }
        assertEquals(1, list.size)
        assertEquals(9, list[0].employeeId)
    }

    @Test
    fun embedded_null(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val list: List<Robot> = db.runQuery {
            QueryDsl.from(r).where { r.info2 eq RobotInfo2(salary = BigDecimal(3000)) }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(8, 13), list.map { it.employeeId })
    }
}
